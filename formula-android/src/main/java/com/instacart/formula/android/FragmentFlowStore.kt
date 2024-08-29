package com.instacart.formula.android

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.RuntimeConfig
import com.instacart.formula.Snapshot
import com.instacart.formula.android.internal.Binding
import com.instacart.formula.android.events.FragmentLifecycleEvent
import com.instacart.formula.android.internal.CompositeBinding
import com.instacart.formula.android.internal.FeatureObservableAction
import com.instacart.formula.android.utils.MainThreadDispatcher
import com.instacart.formula.rxjava3.RxAction
import com.instacart.formula.rxjava3.toObservable
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable

/**
 * A FragmentFlowStore is responsible for managing the state of multiple [FragmentKey] instances.
 */
class FragmentFlowStore @PublishedApi internal constructor(
    private val root: CompositeBinding<*>,
) : Formula<FragmentEnvironment, FragmentFlowState, FragmentFlowState>() {
    companion object {
        inline fun init(
            crossinline init: FragmentStoreBuilder<Unit>.() -> Unit
        ): FragmentFlowStore {
            return init(Unit, init)
        }

        inline fun <Component> init(
            rootComponent: Component,
            crossinline contracts: FragmentStoreBuilder<Component>.() -> Unit
        ): FragmentFlowStore {
            val bindings = FragmentStoreBuilder.build(contracts)
            val root = CompositeBinding(rootComponent, bindings.bindings)
            return FragmentFlowStore(root)
        }
    }


    private val lifecycleEvents = PublishRelay.create<FragmentLifecycleEvent>()
    private val visibleContractEvents = PublishRelay.create<FragmentId>()
    private val hiddenContractEvents = PublishRelay.create<FragmentId>()

    private val lifecycleEventStream = RxAction.fromObservable { lifecycleEvents }
    private val visibleContractEventStream = RxAction.fromObservable { visibleContractEvents }
    private val hiddenContractEventStream = RxAction.fromObservable { hiddenContractEvents }

    internal fun onLifecycleEffect(event: FragmentLifecycleEvent) {
        lifecycleEvents.accept(event)
    }

    internal fun onVisibilityChanged(contract: FragmentId, visible: Boolean) {
        if (visible) {
            visibleContractEvents.accept(contract)
        } else {
            hiddenContractEvents.accept(contract)
        }
    }

    override fun initialState(input: FragmentEnvironment): FragmentFlowState = FragmentFlowState()

    override fun Snapshot<FragmentEnvironment, FragmentFlowState>.evaluate(): Evaluation<FragmentFlowState> {
        val rootInput = Binding.Input(
            environment = input,
            component = Unit,
            activeFragments = state.activeIds,
            onInitializeFeature = context.onEvent { event ->
                val features = state.features.plus(event.id to event)
                transition(state.copy(features = features))
            }
        )
        root.bind(context, rootInput)

        return Evaluation(
            output = state,
            actions = context.actions {
                lifecycleEventStream.onEvent { event ->
                    val fragmentId = event.fragmentId
                    when (event) {
                        is FragmentLifecycleEvent.Removed -> {
                            val updated = state.copy(
                                activeIds = state.activeIds.minus(fragmentId),
                                states = state.states.minus(fragmentId),
                                features = state.features.minus(fragmentId)
                            )
                            transition(updated)
                        }
                        is FragmentLifecycleEvent.Added -> {
                            if (!state.activeIds.contains(fragmentId)) {
                                if (root.binds(fragmentId.key)) {
                                    val updated = state.copy(activeIds = state.activeIds.plus(fragmentId))
                                    transition(updated)
                                } else {
                                    val updated = state.copy(
                                        activeIds = state.activeIds.plus(fragmentId),
                                        features = state.features.plus(fragmentId to FeatureEvent.MissingBinding(fragmentId))
                                    )
                                    transition(updated)
                                }
                            } else {
                                none()
                            }
                        }
                    }
                }

                visibleContractEventStream.onEvent {
                    if (state.visibleIds.contains(it)) {
                        // TODO: should we log this duplicate visibility event?
                        none()
                    } else {
                        transition(state.copy(visibleIds = state.visibleIds.plus(it)))
                    }
                }

                hiddenContractEventStream.onEvent {
                    transition(state.copy(visibleIds = state.visibleIds.minus(it)))
                }

                state.features.entries.forEach { entry ->
                    val fragmentId = entry.key
                    val feature = (entry.value as? FeatureEvent.Init)?.feature
                    if (feature != null) {
                        val action = FeatureObservableAction(
                            fragmentEnvironment = input,
                            fragmentId = fragmentId,
                            feature = feature,
                        )
                        action.onEvent {
                            if (state.activeIds.contains(fragmentId)) {
                                val keyState = FragmentState(fragmentId.key, it)
                                transition(state.copy(states = state.states.plus(fragmentId to keyState)))
                            } else {
                                none()
                            }
                        }
                    }
                }
            }
        )
    }

    internal fun state(environment: FragmentEnvironment): Observable<FragmentFlowState> {
        val config = RuntimeConfig(
            defaultDispatcher = MainThreadDispatcher(),
        )
        return toObservable(environment, config)
    }
}

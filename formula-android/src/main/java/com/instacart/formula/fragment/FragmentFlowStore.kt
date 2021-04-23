package com.instacart.formula.fragment

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.rxjava3.RxStream
import com.instacart.formula.integration.Binding
import com.instacart.formula.integration.FeatureEvent
import com.instacart.formula.integration.DisposableScope
import com.instacart.formula.integration.FragmentBindingBuilder
import com.instacart.formula.integration.KeyState
import com.instacart.formula.rxjava3.toObservable
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable

/**
 * A FragmentFlowStore is responsible for managing the state of multiple [FragmentContract] instances.
 */
class FragmentFlowStore(
    private val root: Binding<Unit>
) : Formula<FragmentEnvironment, FragmentFlowState, FragmentFlowState> {
    companion object {
        inline fun init(
            crossinline init: FragmentBindingBuilder<Unit>.() -> Unit
        ): FragmentFlowStore {
            return init(Unit, init)
        }

        inline fun <Component> init(
            rootComponent: Component,
            crossinline contracts: FragmentBindingBuilder<Component>.() -> Unit
        ): FragmentFlowStore {
            val factory: (Unit) -> DisposableScope<Component> = {
                DisposableScope(component = rootComponent, onDispose = {})
            }

            val bindings = FragmentBindingBuilder.build(contracts)
            val root = Binding.composite(factory, bindings)
            return FragmentFlowStore(root)
        }
    }


    private val lifecycleEvents = PublishRelay.create<FragmentLifecycleEvent>()
    private val visibleContractEvents = PublishRelay.create<FragmentKey>()
    private val hiddenContractEvents = PublishRelay.create<FragmentKey>()

    private val lifecycleEventStream = RxStream.fromObservable { lifecycleEvents }
    private val visibleContractEventStream = RxStream.fromObservable { visibleContractEvents }
    private val hiddenContractEventStream = RxStream.fromObservable { hiddenContractEvents }

    internal fun onLifecycleEffect(event: FragmentLifecycleEvent) {
        lifecycleEvents.accept(event)
    }

    internal fun onVisibilityChanged(contract: FragmentKey, visible: Boolean) {
        if (visible) {
            visibleContractEvents.accept(contract)
        } else {
            hiddenContractEvents.accept(contract)
        }
    }

    override fun initialState(input: FragmentEnvironment): FragmentFlowState = FragmentFlowState()

    override fun evaluate(
        input: FragmentEnvironment,
        state: FragmentFlowState,
        context: FormulaContext<FragmentFlowState>
    ): Evaluation<FragmentFlowState> {
        val rootInput = Binding.Input(
            environment = input,
            component = Unit,
            activeKeys = state.activeKeys,
            onInitializeFeature = context.eventCallback { event ->
                val features = state.features.plus(event.key to event)
                transition(state.copy(features = features))
            }
        )
        root.bind(context, rootInput)

        return Evaluation(
            output = state,
            updates = context.updates {
                events(lifecycleEventStream) { event ->
                    val key = event.key
                    when (event) {
                        is FragmentLifecycleEvent.Removed -> {
                            val updated = state.copy(
                                activeKeys = state.activeKeys.minus(key),
                                states = state.states.minus(key),
                                features = state.features.minus(key)
                            )
                            transition(updated)
                        }
                        is FragmentLifecycleEvent.Added -> {
                            if (!state.activeKeys.contains(key)) {
                                if (root.binds(key)) {
                                    val updated = state.copy(activeKeys = state.activeKeys.plus(key))
                                    transition(updated)
                                } else {
                                    val updated = state.copy(
                                        activeKeys = state.activeKeys.plus(key),
                                        features = state.features.plus(key to FeatureEvent.MissingBinding(key))
                                    )
                                    transition(updated)
                                }
                            } else {
                                none()
                            }
                        }
                    }
                }

                events(visibleContractEventStream) {
                    if (state.visibleKeys.contains(it)) {
                        // TODO: should we log this duplicate visibility event?
                        none()
                    } else {
                        transition(state.copy(visibleKeys = state.visibleKeys.plus(it)))
                    }
                }

                events(hiddenContractEventStream) {
                    transition(state.copy(visibleKeys = state.visibleKeys.minus(it)))
                }

                state.features.entries.forEach { entry ->
                    val key = entry.key
                    val feature = (entry.value as? FeatureEvent.Init)?.feature
                    if (feature != null) {
                        RxStream.fromObservable(feature) {
                            feature.state.onErrorResumeNext {
                                input.onScreenError(key, it)
                                Observable.empty()
                            }
                        }.onEvent {
                            val keyState = KeyState(key, it)
                            transition(state.copy(states = state.states.plus(key to keyState)))
                        }
                    }
                }
            }
        )
    }

    internal fun state(environment: FragmentEnvironment): Observable<FragmentFlowState> {
        return toObservable(environment)
    }
}

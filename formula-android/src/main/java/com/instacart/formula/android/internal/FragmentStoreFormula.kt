package com.instacart.formula.android.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.android.FeatureEvent
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.FragmentState
import com.instacart.formula.android.FragmentId
import com.instacart.formula.android.FragmentKey
import com.instacart.formula.android.FragmentOutput
import com.instacart.formula.android.events.FragmentLifecycleEvent
import com.instacart.formula.rxjava3.RxAction
import com.jakewharton.rxrelay3.PublishRelay

@PublishedApi
internal class FragmentStoreFormula<in Component>(
    private val component: Component,
    private val bindings: List<FeatureBinding<Component, *>>,
) : Formula<FragmentEnvironment, FragmentState, FragmentState>(){
    private val lifecycleEvents = PublishRelay.create<FragmentLifecycleEvent>()
    private val visibleContractEvents = PublishRelay.create<FragmentId>()
    private val hiddenContractEvents = PublishRelay.create<FragmentId>()

    private val lifecycleEventStream = RxAction.fromObservable { lifecycleEvents }
    private val visibleContractEventStream = RxAction.fromObservable { visibleContractEvents }
    private val hiddenContractEventStream = RxAction.fromObservable { hiddenContractEvents }

    fun onLifecycleEffect(event: FragmentLifecycleEvent) {
        lifecycleEvents.accept(event)
    }

    fun onVisibilityChanged(contract: FragmentId, visible: Boolean) {
        if (visible) {
            visibleContractEvents.accept(contract)
        } else {
            hiddenContractEvents.accept(contract)
        }
    }

    override fun initialState(input: FragmentEnvironment): FragmentState = FragmentState()

    override fun Snapshot<FragmentEnvironment, FragmentState>.evaluate(): Evaluation<FragmentState> {
        return Evaluation(
            output = state,
            actions = context.actions {
                lifecycleEventStream.onEvent { event ->
                    val fragmentId = event.fragmentId
                    when (event) {
                        is FragmentLifecycleEvent.Removed -> {
                            val updated = state.copy(
                                activeIds = state.activeIds.minus(fragmentId),
                                outputs = state.outputs.minus(fragmentId),
                                features = state.features.minus(fragmentId)
                            )
                            transition(updated)
                        }
                        is FragmentLifecycleEvent.Added -> {
                            if (!state.activeIds.contains(fragmentId)) {
                                val feature = initFeature(input, fragmentId)
                                val updated = state.copy(
                                    activeIds = state.activeIds.plus(fragmentId),
                                    features = state.features.plus(feature.id to feature)
                                )
                                transition(updated)
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
                            val keyState = FragmentOutput(fragmentId.key, it)
                            transition(state.copy(outputs = state.outputs.plus(fragmentId to keyState)))
                        }
                    }
                }
            }
        )
    }

    private fun initFeature(
        environment: FragmentEnvironment,
        fragmentId: FragmentId,
    ): FeatureEvent {
        val initialized = try {
            bindings.firstNotNullOfOrNull { binding ->
                if (binding.type.isInstance(fragmentId.key)) {
                    val featureFactory = binding.feature as FeatureFactory<Component, FragmentKey>
                    val feature = environment.fragmentDelegate.initializeFeature(
                        fragmentId = fragmentId,
                        factory = featureFactory,
                        dependencies = component,
                        key = fragmentId.key,
                    )
                    FeatureEvent.Init(fragmentId, feature)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            FeatureEvent.Failure(fragmentId, e)
        }

        return initialized ?: FeatureEvent.MissingBinding(fragmentId)
    }
}
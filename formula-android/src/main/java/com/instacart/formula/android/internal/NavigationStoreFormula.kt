package com.instacart.formula.android.internal

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.Transition
import com.instacart.formula.android.FeatureEvent
import com.instacart.formula.android.RouteEnvironment
import com.instacart.formula.android.NavigationState
import com.instacart.formula.android.RouteId
import com.instacart.formula.android.RouteOutput
import kotlinx.coroutines.flow.MutableSharedFlow
import com.instacart.formula.android.RxJavaFeature
import com.instacart.formula.android.StateFlowFeature
import kotlinx.coroutines.CoroutineDispatcher

@PublishedApi
internal class NavigationStoreFormula(
    private val asyncDispatcher: CoroutineDispatcher,
    private val environment: RouteEnvironment,
) : Formula<Unit, NavigationState, NavigationState>(){
    private val stateChanges = MutableSharedFlow<Transition<Unit, NavigationState, Unit>>(
        extraBufferCapacity = Int.MAX_VALUE,
    )

    fun routeAdded(feature: FeatureEvent) {
        stateChanges.tryEmit {
            if (!state.activeIds.contains(feature.id)) {
                val updated = state.copy(
                    activeIds = state.activeIds.plus(feature.id),
                    features = state.features.plus(feature.id to feature)
                )
                transition(updated)
            } else {
                none()
            }
        }
    }

    fun routeRemoved(routeId: RouteId<*>) {
        stateChanges.tryEmit {
            val updated = state.copy(
                activeIds = state.activeIds.minus(routeId),
                outputs = state.outputs.minus(routeId),
                features = state.features.minus(routeId)
            )
            transition(updated)
        }
    }

    fun routeVisible(routeId: RouteId<*>) {
        stateChanges.tryEmit {
            if (state.visibleIds.contains(routeId)) {
                // TODO: should we log this duplicate visibility event?
                none()
            } else {
                transition(state.copy(visibleIds = state.visibleIds.plus(routeId)))
            }
        }
    }

    fun routeHidden(routeId: RouteId<*>) {
        stateChanges.tryEmit {
            transition(state.copy(visibleIds = state.visibleIds.minus(routeId)))
        }
    }

    override fun initialState(input: Unit): NavigationState = NavigationState()

    override fun Snapshot<Unit, NavigationState>.evaluate(): Evaluation<NavigationState> {
        return Evaluation(
            output = state,
            actions = context.actions {
                Action.fromFlow { stateChanges }.onEvent { update ->
                    delegate(update)
                }

                state.features.entries.forEach { entry ->
                    val routeId = entry.key
                    val feature = (entry.value as? FeatureEvent.Init)?.feature
                    if (feature != null) {
                        val action = when (feature) {
                            is RxJavaFeature -> {
                                FeatureObservableAction(
                                    routeEnvironment = environment,
                                    routeId = routeId,
                                    feature = feature,
                                )
                            }
                            is StateFlowFeature -> {
                                StateFlowFeatureAction(
                                    asyncDispatcher = asyncDispatcher,
                                    routeEnvironment = environment,
                                    routeId = routeId,
                                    feature = feature,
                                )
                            }
                        }

                        action.onEvent {
                            val keyState = RouteOutput(routeId.key, it)
                            transition(state.copy(outputs = state.outputs.plus(routeId to keyState)))
                        }
                    }
                }
            }
        )
    }
}

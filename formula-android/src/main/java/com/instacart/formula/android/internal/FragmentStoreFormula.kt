package com.instacart.formula.android.internal

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.Transition
import com.instacart.formula.android.FeatureEvent
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.FragmentState
import com.instacart.formula.android.FragmentId
import com.instacart.formula.android.FragmentOutput
import kotlinx.coroutines.flow.MutableSharedFlow

@PublishedApi
internal class FragmentStoreFormula : Formula<FragmentEnvironment, FragmentState, FragmentState>(){

    private val stateChanges = MutableSharedFlow<Transition<FragmentEnvironment, FragmentState, Unit>>(
        extraBufferCapacity = Int.MAX_VALUE,
    )

    fun fragmentAdded(feature: FeatureEvent) {
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

    fun fragmentRemoved(fragmentId: FragmentId) {
        stateChanges.tryEmit {
            val updated = state.copy(
                activeIds = state.activeIds.minus(fragmentId),
                outputs = state.outputs.minus(fragmentId),
                features = state.features.minus(fragmentId)
            )
            transition(updated)
        }
    }

    fun fragmentVisible(fragmentId: FragmentId) {
        stateChanges.tryEmit {
            if (state.visibleIds.contains(fragmentId)) {
                // TODO: should we log this duplicate visibility event?
                none()
            } else {
                transition(state.copy(visibleIds = state.visibleIds.plus(fragmentId)))
            }
        }
    }

    fun fragmentHidden(fragmentId: FragmentId) {
        stateChanges.tryEmit {
            transition(state.copy(visibleIds = state.visibleIds.minus(fragmentId)))
        }
    }

    override fun initialState(input: FragmentEnvironment): FragmentState = FragmentState()

    override fun Snapshot<FragmentEnvironment, FragmentState>.evaluate(): Evaluation<FragmentState> {
        return Evaluation(
            output = state,
            actions = context.actions {
                Action.fromFlow { stateChanges }.onEvent { update ->
                    delegate(update)
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
}

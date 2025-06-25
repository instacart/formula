package com.instacart.formula.android.fakes

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.android.FragmentId
import kotlinx.coroutines.flow.MutableSharedFlow

class StateFlowFormula: Formula<FragmentId, String, String>() {
    private val sharedFlow = MutableSharedFlow<Pair<String, String>>(
        extraBufferCapacity = Int.MAX_VALUE,
    )

    fun publish(instanceId: String, state: String) {
        sharedFlow.tryEmit(instanceId to state)
    }

    override fun initialState(input: FragmentId): String {
        return ""
    }

    override fun Snapshot<FragmentId, String>.evaluate(): Evaluation<String> {
        return Evaluation(
            output = state,
            actions = context.actions {
                Action.fromFlow { sharedFlow }.onEvent { (instanceId, value) ->
                    if (instanceId == input.instanceId) {
                        transition(value)
                    } else {
                        none()
                    }
                }
            }
        )
    }
}
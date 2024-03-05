package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.Transition

class EventCallbackFormula(
    private val executionType: Transition.ExecutionType? = null
) : Formula<Unit, String, EventCallbackFormula.Output>() {

    data class Output(
        val state: String,
        val changeState: (String) -> Unit
    )

    override fun initialState(input: Unit): String = ""

    override fun Snapshot<Unit, String>.evaluate(): Evaluation<Output> {
        val changeState: (String) -> Unit = if (executionType == null) {
            context.onEvent { newState ->
                transition(newState)
            }
        } else {
            context.onEventWithExecutionType(executionType) { newState ->
                transition(newState)
            }
        }
        return Evaluation(
            output = Output(
                state = state,
                changeState = changeState,
            )
        )
    }
}

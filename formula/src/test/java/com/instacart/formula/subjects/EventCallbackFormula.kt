package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot

class EventCallbackFormula : Formula<Unit, String, EventCallbackFormula.Output>() {

    data class Output(
        val state: String,
        val changeState: (String) -> Unit
    )

    override fun initialState(input: Unit): String = ""

    override fun Snapshot<Unit, String>.evaluate(): Evaluation<Output> {
        return Evaluation(
            output = Output(
                state = state,
                changeState = context.onEvent { newState ->
                    transition(newState)
                }
            )
        )
    }
}

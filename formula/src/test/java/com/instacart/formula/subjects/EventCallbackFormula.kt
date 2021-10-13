package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext

class EventCallbackFormula : Formula<Unit, String, EventCallbackFormula.Output>() {

    data class Output(
        val state: String,
        val changeState: (String) -> Unit
    )

    override fun initialState(input: Unit): String = ""

    override fun evaluate(input: Unit, state: String, context: FormulaContext<String>): Evaluation<Output> {
        return Evaluation(
            output = Output(
                state = state,
                changeState = context.onEvent<String> { newState ->
                    transition(newState)
                }
            )
        )
    }
}

package com.instacart.formula

class EventCallbackFormula : Formula<Unit, String, EventCallbackFormula.Output> {

    data class Output(
        val state: String,
        val changeState: (String) -> Unit
    )

    override fun initialState(input: Unit): String = ""

    override fun evaluate(input: Unit, state: String, context: FormulaContext<String>): Evaluation<Output> {
        return Evaluation(
            output = Output(
                state = state,
                changeState = context.eventCallback { newState ->
                    transition(newState)
                }
            )
        )
    }
}

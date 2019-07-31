package com.instacart.formula

class EventCallbackFormula : Formula<Unit, String, Unit, EventCallbackFormula.RenderModel> {

    data class RenderModel(
        val state: String,
        val changeState: (String) -> Unit
    )

    override fun initialState(input: Unit): String = ""

    override fun evaluate(input: Unit, state: String, context: FormulaContext<String, Unit>): Evaluation<RenderModel> {
        return Evaluation(
            renderModel = RenderModel(
                state = state,
                changeState = context.eventCallback { newState ->
                    newState.transition()
                }
            )
        )
    }
}

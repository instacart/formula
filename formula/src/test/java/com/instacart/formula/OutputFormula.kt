package com.instacart.formula

class OutputFormula : Formula<Unit, Int, OutputFormula.Output, OutputFormula.RenderModel> {

    class Output(val state: Int)

    class RenderModel(
        val childState: Int,
        val triggerOutput: () -> Unit,
        val incrementAndOutput: () -> Unit
    )

    override fun initialState(input: Unit): Int = 0

    override fun evaluate(input: Unit, state: Int, context: FormulaContext<Int, Output>): Evaluation<RenderModel> {
        return Evaluation(
            renderModel = RenderModel(
                childState = state,
                triggerOutput = context.callback("trigger output") {
                    output(Output(state))
                },
                incrementAndOutput = context.callback("increment and trigger output") {
                    val newState = state + 1
                    transition(newState, Output(newState))
                }
            )
        )
    }
}

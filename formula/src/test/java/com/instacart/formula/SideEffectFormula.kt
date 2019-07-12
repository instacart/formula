package com.instacart.formula

class SideEffectFormula : Formula<Unit, Int, Unit, SideEffectFormula.RenderModel> {
    class RenderModel(
        val transition: () -> Unit
    )

    override fun initialState(input: Unit): Int = 0

    override fun evaluate(input: Unit, state: Int, context: FormulaContext<Int, Unit>): Evaluation<RenderModel> {
        return Evaluation(
            renderModel = RenderModel(
                transition = context.callback {
                    sideEffect("no state change") {
                        // noop
                    }
                }
            )
        )
    }
}

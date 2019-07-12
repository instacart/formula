package com.instacart.formula

class SideEffectFormula(
    private val onSideEffect: () -> Unit
) : Formula<Unit, Int, Unit, SideEffectFormula.RenderModel> {

    class RenderModel(
        val triggerSideEffect: () -> Unit
    )

    override fun initialState(input: Unit): Int = 0

    override fun evaluate(input: Unit, state: Int, context: FormulaContext<Int, Unit>): Evaluation<RenderModel> {
        return Evaluation(
            renderModel = RenderModel(
                triggerSideEffect = context.callback {
                    sideEffect("no state change", onSideEffect)
                }
            )
        )
    }
}

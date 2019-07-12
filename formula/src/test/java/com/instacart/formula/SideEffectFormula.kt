package com.instacart.formula

class SideEffectFormula(
    private val sideEffectService: SideEffectService
) : Formula<Unit, Int, Unit, SideEffectFormula.RenderModel> {

    class RenderModel(
        val sideEffectTransition: () -> Unit
    )

    override fun initialState(input: Unit): Int = 0

    override fun evaluate(input: Unit, state: Int, context: FormulaContext<Int, Unit>): Evaluation<RenderModel> {
        return Evaluation(
            renderModel = RenderModel(
                sideEffectTransition = context.callback {
                    sideEffect("no state change", sideEffectService)
                }
            )
        )
    }
}

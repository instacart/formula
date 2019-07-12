package com.instacart.formula

class ChildTransitionAfterNoEvaluationPass :
    Formula<Unit, Int, Unit, ChildTransitionAfterNoEvaluationPass.RenderModel> {

    class RenderModel(
        val child: SideEffectFormula.RenderModel
    )

    private val child = SideEffectFormula()

    override fun initialState(input: Unit): Int = 0

    override fun evaluate(input: Unit, state: Int, context: FormulaContext<Int, Unit>): Evaluation<RenderModel> {
        return Evaluation(
            renderModel = RenderModel(
                child = context.child(child, Unit)
            )
        )
    }
}

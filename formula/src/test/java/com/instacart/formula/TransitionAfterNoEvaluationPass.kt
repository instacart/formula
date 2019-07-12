package com.instacart.formula

class TransitionAfterNoEvaluationPass : Formula<Unit, Int, Unit, TransitionAfterNoEvaluationPass.RenderModel> {

    class RenderModel(
        val transition: () -> Unit
    )

    override fun initialState(input: Unit): Int = 0

    override fun evaluate(input: Unit, state: Int, context: FormulaContext<Int, Unit>): Evaluation<RenderModel> {
        return Evaluation(
            renderModel = RenderModel(
                transition = context.callback {
                    sideEffect("side effect doesn't invalidate state") {
                        // noop
                    }
                }
            )
        )
    }
}

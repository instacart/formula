package com.instacart.formula

class NestedChildTransitionAfterNoEvaluationPass :
    Formula<Unit, Int, Unit, NestedChildTransitionAfterNoEvaluationPass.RenderModel> {

    class RenderModel(
        val child: Child.RenderModel
    )

    class Child : Formula<Unit, Int, Unit, Child.RenderModel> {
        class RenderModel(
            val child: SideEffectFormula.RenderModel
        )

        override fun initialState(input: Unit): Int = 0

        override fun evaluate(input: Unit, state: Int, context: FormulaContext<Int, Unit>): Evaluation<RenderModel> {
            return Evaluation(
                renderModel = RenderModel(
                    child = context.child(SideEffectFormula(), Unit)
                )
            )
        }
    }

    private val child = Child()

    override fun initialState(input: Unit): Int = 0

    override fun evaluate(input: Unit, state: Int, context: FormulaContext<Int, Unit>): Evaluation<RenderModel> {
        return Evaluation(
            renderModel = RenderModel(
                child = context.child(child, Unit)
            )
        )
    }
}

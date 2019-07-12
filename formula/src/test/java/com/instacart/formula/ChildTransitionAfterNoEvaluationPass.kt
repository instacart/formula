package com.instacart.formula

class ChildTransitionAfterNoEvaluationPass :
    Formula<Unit, Int, Unit, ChildTransitionAfterNoEvaluationPass.RenderModel> {

    class RenderModel(
        val child: Child.RenderModel
    )

    class Child : Formula<Unit, Int, Unit, Child.RenderModel> {
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

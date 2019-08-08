package com.instacart.formula

object UsingKeyToScopeCallbacksWithinAnotherFunction {

    class ChildRenderModel(
        val callback: () -> Unit
    )

    class TestRenderModel(
        val first: ChildRenderModel,
        val second: ChildRenderModel
    )

    class TestFormula : StatelessFormula<Unit, TestRenderModel>() {
        override fun evaluate(input: Unit, context: FormulaContext<Unit>): Evaluation<TestRenderModel> {
            return Evaluation(
                renderModel = TestRenderModel(
                    first = context.key("first") {
                        createChild(context)
                    },
                    second = context.key("second") {
                        createChild(context)
                    }
                )
            )
        }

        private fun createChild(context: FormulaContext<Unit>): ChildRenderModel {
            return ChildRenderModel(
                callback = context.callback {
                    none()
                }
            )
        }
    }
}

package com.instacart.formula

object UsingCallbacksWithinAnotherFunction {

    fun test() = TestFormula().start().test()

    class TestRenderModel(
        val first: () -> Unit,
        val second: () -> Unit
    )

    class TestFormula : StatelessFormula<Unit, TestRenderModel>() {
        override fun evaluate(input: Unit, context: FormulaContext<Unit>): Evaluation<TestRenderModel> {
            return Evaluation(
                renderModel = TestRenderModel(
                    first = createDefaultCallback(context),
                    second = createDefaultCallback(context)
                )
            )
        }

        private fun createDefaultCallback(context: FormulaContext<Unit>): () -> Unit {
            return context.callback {
                none()
            }
        }
    }
}

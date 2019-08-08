package com.instacart.formula

object UsingCallbacksWithinAnotherFunction {

    fun test() = TestFormula().state(Unit).test()

    class TestRenderModel(
        val first: () -> Unit,
        val second: () -> Unit
    )

    class TestFormula : StatelessFormula<Unit, Unit, TestRenderModel>() {
        override fun evaluate(input: Unit, context: FormulaContext<Unit, Unit>): Evaluation<TestRenderModel> {
            return Evaluation(
                renderModel = TestRenderModel(
                    first = createDefaultCallback(context),
                    second = createDefaultCallback(context)
                )
            )
        }

        private fun createDefaultCallback(context: FormulaContext<Unit, Unit>): () -> Unit {
            return context.callback {
                none()
            }
        }
    }
}

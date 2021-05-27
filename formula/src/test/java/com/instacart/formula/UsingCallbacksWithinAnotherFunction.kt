package com.instacart.formula

import com.instacart.formula.test.test

object UsingCallbacksWithinAnotherFunction {

    fun test() = TestFormula().test(Unit)

    class TestOutput(
        val first: () -> Unit,
        val second: () -> Unit
    )

    class TestFormula : StatelessFormula<Unit, TestOutput>() {
        override fun evaluate(input: Unit, context: FormulaContext<Unit>): Evaluation<TestOutput> {
            return Evaluation(
                output = TestOutput(
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

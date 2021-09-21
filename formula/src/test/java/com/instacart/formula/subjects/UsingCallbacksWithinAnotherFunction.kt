package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.FormulaContext
import com.instacart.formula.StatelessFormula
import com.instacart.formula.test.TestableRuntime

object UsingCallbacksWithinAnotherFunction {

    fun test(runtime: TestableRuntime) = runtime.test(TestFormula(), Unit)

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
            return context.onEvent {
                none()
            }
        }
    }
}

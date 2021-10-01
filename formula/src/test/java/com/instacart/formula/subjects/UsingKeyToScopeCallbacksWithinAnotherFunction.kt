package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.FormulaContext
import com.instacart.formula.Listener
import com.instacart.formula.StatelessFormula

object UsingKeyToScopeCallbacksWithinAnotherFunction {

    class ChildOutput(
        val callback: Listener<Unit>,
    )

    class TestOutput(
        val first: ChildOutput,
        val second: ChildOutput
    )

    class TestFormula : StatelessFormula<Unit, TestOutput>() {
        override fun evaluate(input: Unit, context: FormulaContext<Unit>): Evaluation<TestOutput> {
            return Evaluation(
                output = TestOutput(
                    first = context.key("first") {
                        createChild(context)
                    },
                    second = context.key("second") {
                        createChild(context)
                    }
                )
            )
        }

        private fun createChild(context: FormulaContext<Unit>): ChildOutput {
            return ChildOutput(
                callback = context.onEvent {
                    none()
                }
            )
        }
    }
}

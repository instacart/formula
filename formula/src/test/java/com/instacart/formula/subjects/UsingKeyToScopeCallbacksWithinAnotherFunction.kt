package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.FormulaContext
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula

object UsingKeyToScopeCallbacksWithinAnotherFunction {

    data class ChildOutput(
        val callback: Listener<Unit>,
    )

    data class TestOutput(
        val first: ChildOutput,
        val second: ChildOutput
    )

    class TestFormula : StatelessFormula<Unit, TestOutput>() {
        override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<TestOutput> {
            return Evaluation(
                output = TestOutput(
                    first = context.key("first") {
                        createChild()
                    },
                    second = context.key("second") {
                        createChild()
                    }
                )
            )
        }

        private fun Snapshot<*, Unit>.createChild(): ChildOutput {
            return ChildOutput(
                callback = context.onEvent {
                    none()
                }
            )
        }
    }
}

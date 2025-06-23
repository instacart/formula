package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.test.test

object ReusableFunctionCreatesUniqueListeners {

    fun test() = TestFormula().test().input(Unit)

    data class TestOutput(
        val firstListener: Listener<Unit>,
        val secondListener: Listener<Unit>,
    )

    class TestFormula : StatelessFormula<Unit, TestOutput>() {
        override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<TestOutput> {
            return Evaluation(
                output = TestOutput(
                    firstListener = createDefaultCallback(),
                    secondListener = createDefaultCallback()
                )
            )
        }

        private fun Snapshot<*, Unit>.createDefaultCallback(): Listener<Unit> {
            return context.onEvent {
                none()
            }
        }
    }
}

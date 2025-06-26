package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.test.TestFormulaObserver
import com.instacart.formula.test.test
import com.instacart.formula.types.OnStartActionFormula

class InputChangeWhileFormulaRunningRobot(eventCount: Int) {
    private var input: Int = 0

    private var observer: TestFormulaObserver<Int, Int, Parent>? = null
    private val parent = Parent(
        eventCount = eventCount,
        onAction = {
            input += 1
            observer?.input(input)
        }
    )

    val test = parent.test().apply {
        observer = this
    }

    class Parent(
        private val eventCount: Int,
        private val onAction: () -> Unit,
    ) : StatelessFormula<Int, Int>() {
        private val childFormula: OnStartActionFormula = OnStartActionFormula(eventCount)

        override fun Snapshot<Int, Unit>.evaluate(): Evaluation<Int> {
            context.child(
                formula = childFormula,
                input = OnStartActionFormula.Input(
                    onAction = onAction,
                )
            )

            return Evaluation(
                output = input,
            )
        }
    }
}
package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.test.test

object UniqueListenersWithinLoop {

    fun test() = TestFormula().test().input(Unit)

    data class TestOutput(
        val listeners: List<Listener<Unit>>,
    )

    class TestFormula : StatelessFormula<Unit, TestOutput>() {
        override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<TestOutput> {
            return Evaluation(
                output = TestOutput(
                    listeners = (0..5).map {
                        context.onEvent { none() }
                    }
                )
            )
        }
    }
}

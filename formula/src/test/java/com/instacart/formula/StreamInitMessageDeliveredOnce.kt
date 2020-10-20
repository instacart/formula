package com.instacart.formula

import com.instacart.formula.test.test

object StreamInitMessageDeliveredOnce {
    fun test() = TestFormula().test()

    class TestFormula : StatelessFormula<Unit, Unit>() {
        var timesInitializedCalled = 0

        override fun evaluate(input: Unit, context: FormulaContext<Unit>): Evaluation<Unit> {

            return Evaluation(
                output = Unit,
                updates = context.updates {
                    Stream.onInit().events {
                        transition { timesInitializedCalled += 1 }
                    }
                }
            )
        }
    }
}

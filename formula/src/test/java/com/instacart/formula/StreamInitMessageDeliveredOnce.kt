package com.instacart.formula

import com.instacart.formula.test.test

object StreamInitMessageDeliveredOnce {
    fun test() = TestFormula().test()

    class TestFormula : StatelessFormula<Unit, Unit>() {
        var timesInitializedCalled = 0

        override fun evaluate(input: Unit, context: FormulaContext<Unit>): Evaluation<Unit> {

            return Evaluation(
                renderModel = Unit,
                updates = context.updates {
                    events("init key", Stream.onInit()) {
                        message {
                            timesInitializedCalled += 1
                        }
                    }
                }
            )
        }
    }
}

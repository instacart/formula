package com.instacart.formula

import com.instacart.formula.test.test

object StreamInitMessageDeliveredOnce {
    fun test() = TestFormula().test()

    class TestFormula : StatelessFormula<Unit, Unit>() {
        var effect = 0

        override fun evaluate(input: Unit, context: FormulaContext<Unit>): Evaluation<Unit> {

            return Evaluation(
                renderModel = Unit,
                updates = context.updates {
                    events("effect", Stream.onInit()) {
                        message {
                            effect += 1
                        }
                    }
                }
            )
        }
    }
}

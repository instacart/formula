package com.instacart.formula.tests

import com.instacart.formula.Evaluation
import com.instacart.formula.FormulaContext
import com.instacart.formula.StatelessFormula
import com.instacart.formula.Stream
import com.instacart.formula.test.TestableRuntime
import java.lang.IllegalStateException

object EmitErrorTest {
    fun test(runtime: TestableRuntime) = runtime.test(MyFormula(), Unit)

    class MyFormula : StatelessFormula<Unit, Unit>() {
        override fun evaluate(input: Unit, context: FormulaContext<Unit>): Evaluation<Unit> {
            return Evaluation(
                output = Unit,
                updates = context.updates {
                    events(Stream.onInit()) {
                        throw IllegalStateException("crashed")
                    }
                }
            )
        }
    }
}
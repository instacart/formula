package com.instacart.formula

import io.reactivex.Observable
import org.junit.Test

class DynamicFormulaInputTest {

    @Test fun `using dynamic input`() {
        TestFormula()
            .state(input = Observable.just(1, 2, 3))
            .test()
            .assertValues(1, 2, 3)
    }

    class TestFormula:  StatelessFormula<Int, Unit, Int>() {
        override fun evaluate(input: Int, context: FormulaContext<Unit, Unit>): Evaluation<Int> {
            return Evaluation(
                renderModel = input
            )
        }
    }
}

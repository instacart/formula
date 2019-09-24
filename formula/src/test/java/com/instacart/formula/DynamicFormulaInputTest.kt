package com.instacart.formula

import io.reactivex.Observable
import org.junit.Test

class DynamicFormulaInputTest {

    @Test fun `using dynamic input`() {
        TestFormula()
            .start(input = Observable.just(1, 2, 3))
            .test()
            .assertValues(1, 2, 3)
    }

    class TestFormula:  StatelessFormula<Int, Int>() {
        override fun evaluate(input: Int, context: FormulaContext<Unit>): Evaluation<Int> {
            return Evaluation(renderModel = input)
        }
    }
}

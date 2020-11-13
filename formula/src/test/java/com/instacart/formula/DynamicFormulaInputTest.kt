package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import io.reactivex.rxjava3.core.Observable
import org.junit.Test

class DynamicFormulaInputTest {

    @Test
    fun `using dynamic input`() {
        TestFormula()
            .test(Observable.just(1, 2, 3))
            .apply {
                assertThat(values()).containsExactly(1, 2, 3).inOrder()
            }
    }

    class TestFormula:  StatelessFormula<Int, Int>() {
        override fun evaluate(input: Int, context: FormulaContext<Unit>): Evaluation<Int> {
            return Evaluation(output = input)
        }
    }
}

package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import io.reactivex.rxjava3.core.Observable
import org.junit.Test

class DynamicFormulaInputTest {

    @Test
    fun `using dynamic input`() {
        formula()
            .test(Observable.just(1, 2, 3))
            .apply {
                assertThat(values()).containsExactly(1, 2, 3).inOrder()
            }
    }

    private fun formula() = Formula.stateless { input: Int, context ->
        Evaluation(output = input)
    }
}

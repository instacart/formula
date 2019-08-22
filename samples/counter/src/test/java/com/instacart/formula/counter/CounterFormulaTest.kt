package com.instacart.formula.counter

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import org.junit.Test

class CounterFormulaTest {

    @Test fun `increment 5 times`() {

        CounterFormula()
            .test(Unit)
            .renderModel { onIncrement() }
            .renderModel { onIncrement() }
            .renderModel { onIncrement() }
            .renderModel { onIncrement() }
            .renderModel { onIncrement() }
            .renderModel {
               assertThat(count).isEqualTo("Count: 5")
            }
    }
}

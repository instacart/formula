package com.instacart.formula.counter

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CounterFormulaTest {

    @Test fun `increment 5 times`() = runTest {
        CounterFormula()
            .test(coroutineContext)
            .input(Unit)
            .output { onIncrement(Unit) }
            .output { onIncrement(Unit) }
            .output { onIncrement(Unit) }
            .output { onIncrement(Unit) }
            .output { onIncrement(Unit) }
            .output {
               assertThat(count).isEqualTo("Count: 5")
            }
    }
}

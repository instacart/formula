package com.instacart.formula.test

import com.google.common.truth.Truth
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class TestCoroutineSchedulerIntegrationTest {

    @Test
    fun `delay within action is advanced by scheduler`() = runTest {
        val formula = DelayFormula(1.seconds)
        val observer = formula.test(this)

        observer.input(Unit)
        observer.output {
            Truth.assertThat(this).isEqualTo(0)
        }

        testScheduler.advanceTimeBy(2.seconds)

        observer.output {
            Truth.assertThat(this).isEqualTo(1)
        }

        observer.dispose()
    }

    @Test
    fun `delay within action is advanced by scheduler when infinite`() = runTest {
        // basically forever
        val formula = DelayFormula(Int.MAX_VALUE.seconds)
        val observer = formula.test(this)

        observer.input(Unit)
        observer.output {
            Truth.assertThat(this).isEqualTo(0)
        }

        testScheduler.advanceUntilIdle()

        observer.output {
            Truth.assertThat(this).isEqualTo(1)
        }

        observer.dispose()
    }
}
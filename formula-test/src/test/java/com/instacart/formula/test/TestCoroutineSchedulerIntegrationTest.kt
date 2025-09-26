package com.instacart.formula.test

import com.google.common.truth.Truth
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class TestCoroutineSchedulerIntegrationTest {

    @Test
    fun `delay within action is advanced by scheduler`() = runTest {
        val scheduler = TestCoroutineScheduler()
        val formula = DelayFormula(1.seconds)
        val observer = formula.test(coroutineScheduler = scheduler)

        observer.input(Unit)
        observer.output {
            Truth.assertThat(this).isEqualTo(0)
        }

        scheduler.advanceTimeBy(2.seconds)

        observer.output {
            Truth.assertThat(this).isEqualTo(1)
        }

        observer.dispose()
    }

    @Test
    fun `delay within action is advanced by scheduler when infinite`() = runTest {
        val scheduler = TestCoroutineScheduler()
        // basically forever
        val formula = DelayFormula(Int.MAX_VALUE.seconds)
        val observer = formula.test(coroutineScheduler = scheduler)

        observer.input(Unit)
        observer.output {
            Truth.assertThat(this).isEqualTo(0)
        }

        scheduler.advanceUntilIdle()

        observer.output {
            Truth.assertThat(this).isEqualTo(1)
        }

        observer.dispose()
    }
}
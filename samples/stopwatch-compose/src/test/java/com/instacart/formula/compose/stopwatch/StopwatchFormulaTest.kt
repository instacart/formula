package com.instacart.formula.compose.stopwatch

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class StopwatchFormulaTest {

    @Test
    fun `ticks while running and stops when paused`() = runTest {
        val observer = StopwatchFormula()
            .test(this)
            .input(Unit)

        observer.output {
            assertThat(timePassed).isEqualTo("0s 00")
            startStopButton.onSelected(Unit)
        }

        testScheduler.advanceTimeBy(11.milliseconds)
        observer.output {
            assertThat(timePassed).isEqualTo("0s 01")
            startStopButton.onSelected(Unit)
        }

        testScheduler.advanceTimeBy(100.milliseconds)
        observer.output {
            assertThat(timePassed).isEqualTo("0s 01")
        }

        observer.dispose()
    }
}

package com.instacart.formula.stopwatch

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class StopwatchFormulaTest {

    @Test
    fun `elapsed time follows boot clock across pause and resume`() = runTest {
        var elapsedSinceBoot = 10_000L
        val observer = StopwatchFormula { elapsedSinceBoot }
            .test(this)
            .input(Unit)

        observer.output {
            assertThat(timePassed).isEqualTo("0s 00")
            startStopButton.onSelected(Unit)
        }

        elapsedSinceBoot += 125
        testScheduler.advanceTimeBy(2.milliseconds)
        observer.output {
            assertThat(timePassed).isEqualTo("0s 12")
            startStopButton.onSelected(Unit)
        }

        elapsedSinceBoot += 10_000
        testScheduler.advanceTimeBy(100.milliseconds)
        observer.output {
            assertThat(timePassed).isEqualTo("0s 12")
            startStopButton.onSelected(Unit)
        }

        elapsedSinceBoot += 250
        testScheduler.advanceTimeBy(2.milliseconds)
        observer.output {
            assertThat(timePassed).isEqualTo("0s 37")
            resetButton.onSelected(Unit)
        }

        observer.output {
            assertThat(timePassed).isEqualTo("0s 00")
        }
        observer.dispose()
    }
}

package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.timer.Timer
import com.instacart.formula.timer.TimerProcessorFormula
import com.instacart.formula.timer.TimerRenderModel
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class StreamTest {
    lateinit var scheduler: TestScheduler
    lateinit var subject: TestObserver<TimerRenderModel>

    @Before fun setup() {
        scheduler = TestScheduler()
        subject = TimerProcessorFormula(Timer(scheduler)).state(Unit).test()
    }

    @Test fun `updates time`() {
        subject
            .apply {
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
            }
            .apply {
                val timeValues = values().map { it.time }
                assertThat(timeValues).containsExactly(
                    "Time: 0",
                    "Time: 1",
                    "Time: 2",
                    "Time: 3"
                )
            }
    }

    @Test fun `stream is removed`() {
        subject
            .apply {
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)

                values().last().onResetSelected()

                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
            }
            .apply {
                val timeValues = values().map { it.time }
                assertThat(timeValues).containsExactly(
                    "Time: 0",
                    "Time: 1",
                    "Time: 2",
                    "Time: 0"
                )
            }
    }
}

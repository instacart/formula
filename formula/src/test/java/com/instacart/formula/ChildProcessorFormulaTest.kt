package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.timer.Timer
import com.instacart.formula.timer.TimerProcessorFormula
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class ChildProcessorFormulaTest {
    lateinit var scheduler: TestScheduler
    lateinit var subject: TestObserver<RootFormula.RenderModel>

    @Before fun setup() {
        scheduler = TestScheduler()
        val timerProcessorFormula = TimerProcessorFormula(Timer(scheduler))
        subject = RootFormula(timerProcessorFormula).state(Unit).test()
    }

    @Test fun `child updates`() {
        subject
            .apply {
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
            }
            .apply {
                val timeValues = values().map { it.timer!!.time }
                assertThat(timeValues).containsExactly(
                    "Time: 0",
                    "Time: 1",
                    "Time: 2",
                    "Time: 3"
                )
            }
    }

    @Test fun `child worker is removed`() {
        subject
            .apply {
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)

                values().last().timer!!.onResetSelected()

                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
            }
            .apply {
                val timeValues = values().map { it.timer!!.time }
                assertThat(timeValues).containsExactly(
                    "Time: 0",
                    "Time: 1",
                    "Time: 2",
                    "Time: 0"
                )
            }
    }

    @Test fun `child is removed through effects`() {
        subject
            .apply {
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)

                values().last().timer!!.onClose()
            }
            .apply {
                assertThat(values().last().timer).isNull()
            }
    }
}

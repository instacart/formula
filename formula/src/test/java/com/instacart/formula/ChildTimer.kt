package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import com.instacart.formula.timer.Timer
import com.instacart.formula.timer.TimerEffect
import com.instacart.formula.timer.TimerFormula
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit

class ChildTimer {
    val scheduler = TestScheduler()
    val timer = Timer(scheduler)

    val subject = OptionalChildFormula(TimerFormula(timer)) { state, output ->
        when (output) {
            TimerEffect.Exit -> Transition.Factory.transition(state.copy(showChild = false))
        }
    }.test()

    fun stepBy(seconds: Long) = apply {
        scheduler.advanceTimeBy(seconds, TimeUnit.SECONDS)
    }

    fun resetTimer() = apply {
        subject.renderModel { child!!.onResetSelected() }
    }

    fun close() = apply {
        subject.renderModel { child!!.onClose() }
    }

    fun assertTimerIsVisible(visible: Boolean) = apply {
        subject.renderModel {
            if (visible) {
                assertThat(this).isNotNull()
            } else {
                assertThat(this).isNull()
            }
        }
    }

    fun assertTimeValues(vararg values: String) = apply {
        val timeValues = subject.values().map { it.child!!.time }
        assertThat(timeValues).containsExactly(*values)
    }
}

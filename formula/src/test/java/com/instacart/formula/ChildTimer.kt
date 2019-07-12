package com.instacart.formula

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

    fun step(seconds: Long) {
        scheduler.advanceTimeBy(seconds, TimeUnit.SECONDS)
    }
}

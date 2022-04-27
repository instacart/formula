package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.rxjava3.RxAction
import com.instacart.formula.test.TestableRuntime
import io.reactivex.rxjava3.core.Observable

object SubscribesToAllUpdatesBeforeDeliveringMessages {

    fun test(runtime: TestableRuntime) = runtime.test(TestFormula(runtime), Unit)

    class TestFormula(runtime: TestableRuntime) : Formula<Unit, Int, Int>() {
        private val initial = RxAction.fromObservable { Observable.just(Unit, Unit, Unit, Unit) }
        private val incrementRelay = runtime.newRelay()

        override fun initialState(input: Unit): Int = 0

        override fun Snapshot<Unit, Int>.evaluate(): Evaluation<Int> {
            return Evaluation(
                output = state,
                actions = context.actions {
                    events(initial) {
                        transition { incrementRelay.triggerEvent() }
                    }

                    incrementRelay.action().onEvent {
                        transition(state + 1)
                    }
                }
            )
        }
    }
}

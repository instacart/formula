package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.rxjava3.RxStream
import com.instacart.formula.test.TestableRuntime
import io.reactivex.rxjava3.core.Observable

object SubscribesToAllUpdatesBeforeDeliveringMessages {

    fun test(runtime: TestableRuntime) = runtime.test(TestFormula(runtime), Unit)

    class TestFormula(runtime: TestableRuntime) : Formula<Unit, Int, Int> {
        private val initial = RxStream.fromObservable { Observable.just(Unit, Unit, Unit, Unit) }
        private val incrementRelay = runtime.newRelay()

        override fun initialState(input: Unit): Int = 0

        override fun evaluate(input: Unit, state: Int, context: FormulaContext<Int>): Evaluation<Int> {
            return Evaluation(
                output = state,
                updates = context.updates {
                    events(initial) {
                        transition { incrementRelay.triggerEvent() }
                    }

                    incrementRelay.stream().onEvent {
                        transition(state + 1)
                    }
                }
            )
        }
    }
}

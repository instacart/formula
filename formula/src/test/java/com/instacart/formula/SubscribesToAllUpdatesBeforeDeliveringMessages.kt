package com.instacart.formula

import com.instacart.formula.test.test
import io.reactivex.rxjava3.core.Observable

object SubscribesToAllUpdatesBeforeDeliveringMessages {

    fun test() = TestFormula().test()

    class TestFormula : Formula<Unit, Int, Int> {
        private val initial = RxStream.fromObservable { Observable.just(Unit, Unit, Unit, Unit) }
        private val incrementRelay = IncrementRelay()

        override fun initialState(input: Unit): Int = 0

        override fun evaluate(input: Unit, state: Int, context: FormulaContext<Int>): Evaluation<Int> {
            return Evaluation(
                output = state,
                updates = context.updates {
                    events(initial) {
                        transition { incrementRelay.triggerIncrement() }
                    }

                    incrementRelay.stream().onEvent {
                        transition(state + 1)
                    }
                }
            )
        }
    }
}

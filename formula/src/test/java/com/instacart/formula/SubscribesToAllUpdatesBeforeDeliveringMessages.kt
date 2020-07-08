package com.instacart.formula

import com.instacart.formula.test.test
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable

object SubscribesToAllUpdatesBeforeDeliveringMessages {

    fun test() = TestFormula().test()

    class TestFormula : Formula<Unit, Int, Int> {
        private val initial = Observable.just(Unit, Unit, Unit, Unit)
        private val incrementRelay: PublishRelay<Unit> = PublishRelay.create()

        override fun initialState(input: Unit): Int = 0

        override fun evaluate(input: Unit, state: Int, context: FormulaContext<Int>): Evaluation<Int> {
            return Evaluation(
                output = state,
                updates = context.updates {
                    events(initial) {
                        transition { incrementRelay.accept(Unit) }
                    }

                    events(incrementRelay) {
                        transition(state + 1)
                    }
                }
            )
        }
    }
}

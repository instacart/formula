package com.instacart.formula

import com.instacart.formula.test.test
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

object SubscribesToAllUpdatesBeforeDeliveringMessages {

    fun test() = TestFormula().test()

    class TestFormula : Formula<Unit, Int, Int> {
        private val initial = Observable.just(Unit, Unit, Unit, Unit)
        private val incrementRelay: PublishRelay<Unit> = PublishRelay.create()

        override fun initialState(input: Unit): Int = 0

        override fun evaluate(input: Unit, state: Int, context: FormulaContext<Int>): Evaluation<Int> {
            return Evaluation(
                renderModel = state,
                updates = context.updates {
                    events("initial", initial) {
                        message(incrementRelay::accept, Unit)
                    }

                    events("increment", incrementRelay) {
                        transition(state + 1)
                    }
                }
            )
        }
    }
}

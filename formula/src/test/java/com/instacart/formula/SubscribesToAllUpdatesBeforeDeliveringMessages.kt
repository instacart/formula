package com.instacart.formula

import com.instacart.formula.test.test
import com.instacart.formula.utils.TestUtils
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

object SubscribesToAllUpdatesBeforeDeliveringMessages {

    fun test() = create().test()

    private fun create(): Formula<Unit, Int, Int> {
        val initial = Observable.just(Unit, Unit, Unit, Unit)
        val incrementRelay: PublishRelay<Unit> = PublishRelay.create()

        return TestUtils.create(0) { state, context ->
            Evaluation(
                renderModel = state,
                updates = context.updates {
                    events(initial) {
                        message(incrementRelay::accept, Unit)
                    }

                    events(incrementRelay) {
                        transition(state + 1)
                    }
                }
            )
        }
    }
}

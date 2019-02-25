package com.instacart.formula

import io.reactivex.Flowable
import io.reactivex.subscribers.TestSubscriber

object TestUtils {

    fun <State> bind(
        initial: State,
        changes: Flowable<NextReducer<State, Unit>>,
        subscriber: TestSubscriber<State>
    ) {
        changes
            .scan(initial) { acc, reducer ->
                val next = reducer(acc)
                next.state
            }
            .subscribe(subscriber)
    }
}

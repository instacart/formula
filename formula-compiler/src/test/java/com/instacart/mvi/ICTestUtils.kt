package com.instacart.mvi

import com.instacart.client.mvi.state.NextReducer
import io.reactivex.Flowable
import io.reactivex.subscribers.TestSubscriber

object ICTestUtils {

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
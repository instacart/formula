package com.instacart.formula

import io.reactivex.Observable
import io.reactivex.observers.TestObserver

object TestUtils {

    fun <State> bind(
        initial: State,
        changes: Observable<NextReducer<State, Unit>>
    ) : TestObserver<State> {
        return changes
            .scan(initial) { acc, reducer ->
                val next = reducer(acc)
                next.state
            }
            .test()
    }
}

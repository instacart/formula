package com.instacart.client.mvi.utils

import io.reactivex.Flowable


fun <T> Flowable<(T) -> T>.reduce(initial: T): Flowable<T> {
    return this
        .scan(initial) { state, reducer ->
            reducer(state)
        }
        // Prevent noisy updates
        .distinctUntilChanged()
}
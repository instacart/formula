package com.instacart.formula

import io.reactivex.Flowable

/**
 * Invokes function only if the data has changed.
 */
fun <T> ((T) -> Unit).memoize(): (T) -> Unit {
    return cached(this)
}

fun <T, R> cached(factory: (T) -> R): (T) -> R {
    var last: Pair<T, R>? = null
    return {
        val local = last
        val result: Pair<T, R> = if (local == null || local.first != it) {
            Pair(it, factory(it))
        } else {
            local
        }

        last = result
        result.second
    }
}

fun <T> Flowable<(T) -> T>.reduce(initial: T): Flowable<T> {
    return this
        .scan(initial) { state, reducer ->
            reducer(state)
        }
        // Prevent noisy updates
        .distinctUntilChanged()
}

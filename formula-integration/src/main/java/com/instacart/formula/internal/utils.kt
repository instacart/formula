package com.instacart.formula.internal

import io.reactivex.Flowable

inline fun <T, O> Flowable<T>.mapNotNull(crossinline transform: (T) -> O?): Flowable<O> {
    return flatMap {
        transform(it)?.let { transformed ->
            Flowable.just(transformed)
        } ?: Flowable.empty()
    }
}

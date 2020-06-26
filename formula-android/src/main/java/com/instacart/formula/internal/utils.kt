package com.instacart.formula.internal

import io.reactivex.rxjava3.core.Observable

inline fun <T, O> Observable<T>.mapNotNull(crossinline transform: (T) -> O?): Observable<O> {
    return flatMap {
        transform(it)?.let { transformed ->
            Observable.just(transformed)
        } ?: Observable.empty()
    }
}

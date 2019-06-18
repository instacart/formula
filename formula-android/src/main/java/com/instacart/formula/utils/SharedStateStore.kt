package com.instacart.formula.utils

import com.instacart.formula.internal.mapNotNull
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable

class SharedStateStore<T> {

    private val stateRelay = BehaviorRelay.create<T>()

    fun update(state: T) {
        stateRelay.accept(state)
    }

    fun stateChanges(): Observable<T> {
        return stateRelay
    }

    /**
     * Select a child state object.
     */
    fun <K> select(select: (T) -> K?): Observable<K> {
        return stateRelay
            .mapNotNull(select)
            .distinctUntilChanged()
    }

    /**
     * Imperatively get the current state. Prefer using [stateChanges] instead.
     */
    fun state(): T? = stateRelay.value
}

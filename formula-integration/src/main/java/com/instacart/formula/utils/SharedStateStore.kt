package com.instacart.formula.utils

import com.instacart.formula.internal.mapNotNull
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

class SharedStateStore<T> {

    private val stateRelay = BehaviorRelay.create<T>()

    fun update(state: T) {
        stateRelay.accept(state)
    }

    fun stateChanges(): Flowable<T> {
        return stateRelay.toFlowable(BackpressureStrategy.LATEST)
    }

    /**
     * Select a child state object.
     */
    fun <K> select(select: (T) -> K?): Flowable<K> {
        return stateRelay
            .toFlowable(BackpressureStrategy.LATEST)
            .mapNotNull(select)
            .distinctUntilChanged()
    }

    /**
     * Imperatively get the current state. Prefer using [stateChanges] instead.
     */
    fun state(): T? = stateRelay.value
}

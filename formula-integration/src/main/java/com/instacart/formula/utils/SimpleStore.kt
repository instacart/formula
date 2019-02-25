package com.instacart.formula.utils

import com.instacart.formula.Reducer
import com.instacart.formula.internal.mapNotNull
import com.instacart.formula.reduce
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

class SimpleStore<T> {

    private val stateRelay = BehaviorRelay.create<T>()

    fun stateChanges(): Flowable<T> {
        return stateRelay.toFlowable(BackpressureStrategy.LATEST)
    }

    fun update(state: T) {
        stateRelay.accept(state)
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

    fun observe(initialState: T, reducers: Flowable<Reducer<T>>): Flowable<T> {
        return reducers
            .reduce(initialState)
            .doOnNext(stateRelay)
    }

    /**
     * Imperatively get the current state. Prefer using [stateChanges] instead.
     */
    fun state(): T? = stateRelay.value
}

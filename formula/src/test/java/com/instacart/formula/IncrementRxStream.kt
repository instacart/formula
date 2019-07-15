package com.instacart.formula

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

class IncrementRxStream : RxStream<Unit, Unit> {
    private val relay = PublishRelay.create<Unit>()

    override fun observable(input: Unit): Observable<Unit> {
        return relay
    }

    fun triggerIncrement() = relay.accept(Unit)
}

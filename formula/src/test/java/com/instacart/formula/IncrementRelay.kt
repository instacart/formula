package com.instacart.formula

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

class IncrementRelay {
    private val relay = PublishRelay.create<Unit>()

    fun stream() = RxStream.fromObservable { relay }

    fun triggerIncrement() = relay.accept(Unit)
}

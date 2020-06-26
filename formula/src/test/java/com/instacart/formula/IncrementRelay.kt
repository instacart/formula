package com.instacart.formula

import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable

class IncrementRelay {
    private val relay = PublishRelay.create<Unit>()

    fun stream() = RxStream.fromObservable { relay }

    fun triggerIncrement() = relay.accept(Unit)
}

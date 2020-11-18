package com.instacart.formula

import com.instacart.formula.rxjava3.RxStream
import com.jakewharton.rxrelay3.PublishRelay

class IncrementRelay {
    private val relay = PublishRelay.create<Unit>()

    fun stream() = RxStream.fromObservable { relay }

    fun triggerIncrement() = relay.accept(Unit)
}

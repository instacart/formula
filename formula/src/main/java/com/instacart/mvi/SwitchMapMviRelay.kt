package com.instacart.mvi

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

internal class SwitchMapMviRelay<Effect, Event>(
    val handler: (Effect) -> Flowable<Event>
) : MviRelay<Effect, Event> {

    private val effectRelay = BehaviorRelay.create<Effect>()

    override fun handle(effect: Effect) {
        effectRelay.accept(effect)
    }

    override fun events(): Flowable<Event> {
        return effectRelay
            .toFlowable(BackpressureStrategy.LATEST)
            .switchMap(handler)
    }
}
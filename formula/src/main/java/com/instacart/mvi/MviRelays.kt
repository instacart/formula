package com.instacart.mvi

import io.reactivex.Flowable

object MviRelays {

    /**
     * Creates a new [MviRelay] which passes effects to the handler.
     *
     * This function should be used when an effect should trigger a
     * new network operation while cancelling previously running operation.
     *
     * ```
     * MviRelays.switchMap { params: MyParams ->
     *   repo.fetchMyData(params)
     * }
     * ```
     */
    fun <Effect, Event> switchMap(handler: (Effect) -> Flowable<Event>): MviRelay<Effect, Event> {
        return SwitchMapMviRelay(handler)
    }
}
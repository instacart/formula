package com.instacart.mvi

import io.reactivex.Flowable

/**
 * Producer provides a stream of events or state changes to listen to. It usually returns
 * a hot stream which means that you can miss some events if you are not subscribed.
 *
 * [Event] - type of event that will be produced.
 */
interface Producer<Event> {

    fun events(): Flowable<Event>
}

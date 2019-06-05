package com.instacart.formula

import io.reactivex.Observable

/**
 * Producer provides a stream of events or state changes to listen to. It usually returns
 * a hot stream which means that you can miss some events if you are not subscribed. See [Observable] for more info
 *
 * @param Event type of event that will be produced.
 */
interface Producer<Event> {

    /**
     * An Observable stream of all events being produced by this producer.
     */
    fun events(): Observable<Event>
}

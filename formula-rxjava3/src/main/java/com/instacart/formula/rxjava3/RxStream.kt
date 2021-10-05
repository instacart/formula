package com.instacart.formula.rxjava3

import com.instacart.formula.Cancelable
import com.instacart.formula.Stream
import io.reactivex.rxjava3.core.Observable

/**
 * Formula [Stream] adapter to enable RxJava use.
 */
interface RxStream<Event> : Stream<Event> {
    companion object {
        /**
         * Creates a [Stream] from an [Observable] factory [create].
         *
         * ```
         * events(RxStream.fromObservable { locationManager.updates() }) { event ->
         *   transition()
         * }
         * ```
         */
        inline fun <Event> fromObservable(
            crossinline create: () -> Observable<Event>
        ): Stream<Event> {
            return object : RxStream<Event> {

                override fun observable(): Observable<Event> {
                    return create()
                }

                override fun key(): Any = Unit
            }
        }

        /**
         * Creates a [Stream] from an [Observable] factory [create].
         *
         * ```
         * events(RxStream.fromObservable(itemId) { repo.fetchItem(itemId) }) { event ->
         *   transition()
         * }
         * ```
         *
         * @param key Used to distinguish this [Stream] from other streams.
         */
        inline fun <Event> fromObservable(
            key: Any?,
            crossinline create: () -> Observable<Event>
        ): Stream<Event> {
            return object : RxStream<Event> {

                override fun observable(): Observable<Event> {
                    return create()
                }

                override fun key(): Any? = key
            }
        }
    }

    fun observable(): Observable<Event>

    override fun start(send: (Event) -> Unit): Cancelable? {
        val disposable = observable().subscribe(send)
        return Cancelable(disposable::dispose)
    }
}

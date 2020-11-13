package com.instacart.formula.rxjava3

import com.instacart.formula.Cancelable
import com.instacart.formula.Stream
import io.reactivex.rxjava3.core.Observable

/**
 * Formula [Stream] adapter to enable RxJava use.
 */
interface RxStream<Message> : Stream<Message> {
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
        inline fun <Message> fromObservable(
            crossinline create: () -> Observable<Message>
        ): Stream<Message> {
            return object : RxStream<Message> {

                override fun observable(): Observable<Message> {
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
        inline fun <Message> fromObservable(
            key: Any,
            crossinline create: () -> Observable<Message>
        ): Stream<Message> {
            return object : RxStream<Message> {

                override fun observable(): Observable<Message> {
                    return create()
                }

                override fun key(): Any = key
            }
        }
    }

    fun observable(): Observable<Message>

    override fun start(send: (Message) -> Unit): Cancelable? {
        val disposable = observable().subscribe(send)
        return Cancelable(disposable::dispose)
    }
}

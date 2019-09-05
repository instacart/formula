package com.instacart.formula

import io.reactivex.Observable

/**
 * Formula [Stream] adapter to enable RxJava use.
 */
interface RxStream<Message> : Stream<Message> {
    companion object {
        /**
         * Creates a [Stream] from an [Observable] factory [create] which doesn't take any parameters.
         *
         * ```
         * events(RxStream.fromObservable { locationManager.updates() }) { event ->
         *   transition()
         * }
         * ```
         *
         * @param key Used to distinguish this [Stream] from other streams.
         */
        inline fun <Message> fromObservable(
            key: Any = Unit,
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

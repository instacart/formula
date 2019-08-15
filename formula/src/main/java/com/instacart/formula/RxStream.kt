package com.instacart.formula

import io.reactivex.Observable

/**
 * Formula [Stream] adapter to enable RxJava use.
 */
interface RxStream<Parameter, Message> : Stream<Parameter, Message> {
    companion object {
        /**
         * Creates a [Stream] from an [Observable] factory [create] which doesn't take any parameters.
         *
         * ```
         * events(RxStream.fromObservable { locationManager.updates() }) { event ->
         *   transition()
         * }
         * ```
         */
        inline fun <Message> fromObservable(
            crossinline create: () -> Observable<Message>
        ): Stream<Unit, Message> {
            return object : RxStream<Unit, Message> {
                override fun observable(parameter: Unit): Observable<Message> {
                    return create()
                }
            }
        }

        /**
         * Creates a [Stream] from an [Observable] factory [create] which takes a single [Parameter].
         *
         * ```
         * events(RxStream.withParameter(itemRepository::fetchItem), itemId) {
         *   transition()
         * }
         * ```
         */
        inline fun <Parameter, Message> withParameter(
            crossinline create: (Parameter) -> Observable<Message>
        ): Stream<Parameter, Message> {
            return object : RxStream<Parameter, Message> {
                override fun observable(parameter: Parameter): Observable<Message> {
                    return create(parameter)
                }
            }
        }
    }

    fun observable(parameter: Parameter): Observable<Message>

    override fun start(parameter: Parameter, send: (Message) -> Unit): Cancelable? {
        val disposable = observable(parameter).subscribe(send)
        return Cancelable(disposable::dispose)
    }
}

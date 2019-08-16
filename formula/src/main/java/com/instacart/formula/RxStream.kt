package com.instacart.formula

import io.reactivex.Observable

interface RxStream<Input, Message> : Stream<Input, Message> {
    companion object {
        inline fun <Input, Message> fromObservable(
            crossinline create: (Input) -> Observable<Message>
        ): RxStream<Input, Message> {
            return object : RxStream<Input, Message> {
                override fun observable(input: Input): Observable<Message> {
                    return create(input)
                }
            }
        }
    }

    fun observable(input: Input): Observable<Message>

    override fun start(input: Input, send: (Message) -> Unit): Cancelable? {
        val disposable = observable(input).subscribe(send)
        return Cancelable(disposable::dispose)
    }
}

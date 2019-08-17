package com.instacart.formula

import io.reactivex.Observable

interface RxStream<Data, Message> : Stream<Data, Message> {
    companion object {
        inline fun <Data, Message> fromObservable(
            crossinline create: (Data) -> Observable<Message>
        ): RxStream<Data, Message> {
            return object : RxStream<Data, Message> {
                override fun observable(data: Data): Observable<Message> {
                    return create(data)
                }
            }
        }
    }

    fun observable(data: Data): Observable<Message>

    override fun start(data: Data, send: (Message) -> Unit): Cancelable? {
        val disposable = observable(data).subscribe(send)
        return Cancelable(disposable::dispose)
    }
}

package com.instacart.formula

import io.reactivex.Observable

interface RxStream<Input, Output> : Stream<Input, Output> {
    companion object {
        inline fun <Output> fromObservable(crossinline create: () -> Observable<Output>): RxStream<Unit, Output> {
            return object : RxStream<Unit, Output> {
                override fun observable(input: Unit): Observable<Output> {
                    return create()
                }
            }
        }
    }

    fun observable(input: Input): Observable<Output>

    override fun perform(input: Input, onEvent: (Output) -> Unit): Cancelation? {
        val disposable = observable(input).subscribe(onEvent)
        return Cancelation(disposable::dispose)
    }
}

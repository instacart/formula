package com.instacart.formula

import io.reactivex.Observable

interface RxStream<Input, Output> : Stream<Input, Output> {

    fun observable(input: Input): Observable<Output>

    override fun perform(input: Input, onEvent: (Output) -> Unit): Cancelation? {
        val disposable = observable(input).subscribe(onEvent)
        return Cancelation(disposable::dispose)
    }
}

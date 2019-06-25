package com.instacart.formula

import io.reactivex.Observable
import io.reactivex.disposables.Disposable

interface RxStream<Input, Output> : Stream<Input, Output> {

    fun observable(input: Input): Observable<Output>

    override fun subscribe(input: Input, onEvent: (Output) -> Unit): Disposable {
        return observable(input).subscribe(onEvent)
    }
}

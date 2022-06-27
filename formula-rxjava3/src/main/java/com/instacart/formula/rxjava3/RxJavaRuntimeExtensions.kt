package com.instacart.formula.rxjava3

import com.instacart.formula.IFormula
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.Executor
import java.util.concurrent.Executors

fun <Output : Any> IFormula<Unit, Output>.toObservable(): Observable<Output> {
    return toObservable(input = Unit)
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toObservable(
    input: Input
): Observable<Output> {
    return toObservable(input = Observable.just(input))
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toObservable(
    input: Observable<Input>,
    executor: Executor = Executors.newSingleThreadExecutor(),
): Observable<Output> {
    return RxJavaRuntime.start(input = input, formula = this, executor = executor)
}

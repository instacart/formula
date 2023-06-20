package com.instacart.formula.rxjava3

import com.instacart.formula.IFormula
import io.reactivex.rxjava3.core.Observable

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
    isValidationEnabled: Boolean = false,
): Observable<Output> {
    return RxJavaRuntime.start(input = input, formula = this, isValidationEnabled = isValidationEnabled)
}

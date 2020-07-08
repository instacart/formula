package com.instacart.formula

import io.reactivex.rxjava3.core.Observable

fun <Output : Any> IFormula<Unit, Output>.start(): Observable<Output> {
    return start(input = Unit)
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.start(
    input: Input
): Observable<Output> {
    return start(input = Observable.just(input))
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.start(
    input: Observable<Input>
): Observable<Output> {
    return FormulaRuntime.start(input = input, formula = this)
}

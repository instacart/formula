package com.instacart.formula

import io.reactivex.rxjava3.core.Observable

fun <RenderModel : Any> IFormula<Unit, RenderModel>.start(): Observable<RenderModel> {
    return start(input = Unit)
}

fun <Input : Any, RenderModel : Any> IFormula<Input, RenderModel>.start(
    input: Input
): Observable<RenderModel> {
    return start(input = Observable.just(input))
}

fun <Input : Any, RenderModel : Any> IFormula<Input, RenderModel>.start(
    input: Observable<Input>
): Observable<RenderModel> {
    return FormulaRuntime.start(input = input, formula = this)
}

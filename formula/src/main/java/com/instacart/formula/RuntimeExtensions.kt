package com.instacart.formula

import io.reactivex.rxjava3.core.Observable

fun <State, RenderModel : Any> Formula<Unit, State, RenderModel>.start(): Observable<RenderModel> {
    return start(input = Unit)
}

fun <Input : Any, State, RenderModel : Any> Formula<Input, State, RenderModel>.start(
    input: Input
): Observable<RenderModel> {
    return start(input = Observable.just(input))
}

fun <Input : Any, State, RenderModel : Any> Formula<Input, State, RenderModel>.start(
    input: Observable<Input>
): Observable<RenderModel> {
    return FormulaRuntime.start(input = input, formula = this)
}

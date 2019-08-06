package com.instacart.formula

import io.reactivex.Observable

fun <Input : Any, State, RenderModel : Any> Formula<Input, State, RenderModel>.state(
    input: Input
): Observable<RenderModel> {
    return state(input = Observable.just(input))
}

fun <Input : Any, State, RenderModel : Any> Formula<Input, State, RenderModel>.state(
    input: Observable<Input>
): Observable<RenderModel> {
    return FormulaRuntime.start(input = input, formula = this)
}

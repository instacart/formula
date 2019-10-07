package com.instacart.formula

import io.reactivex.Observable

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

@Deprecated(message = "Use start instead of state", replaceWith = ReplaceWith("start(input)", "com.instacart.formula.start"))
fun <Input : Any, State, RenderModel : Any> Formula<Input, State, RenderModel>.state(
    input: Input
): Observable<RenderModel> {
    return start(input = Observable.just(input))
}

@Deprecated(message = "Use start instead of state", replaceWith = ReplaceWith("start(input)", "com.instacart.formula.start"))
fun <Input : Any, State, RenderModel : Any> Formula<Input, State, RenderModel>.state(
    input: Observable<Input>
): Observable<RenderModel> {
    return FormulaRuntime.start(input = input, formula = this)
}

package com.instacart.formula

import io.reactivex.Observable

fun <Input, State, RenderModel> Formula<Input, State, Unit, RenderModel>.state(
    input: Input
): Observable<RenderModel> {
    return state(
        input = Observable.just(input),
        onEvent = {}
    )
}

fun <Input, State, Output, RenderModel> Formula<Input, State, Output, RenderModel>.state(
    input: Input,
    onEvent: (Output) -> Unit
): Observable<RenderModel> {
    return state(
        input = Observable.just(input),
        onEvent = onEvent
    )
}

fun <Input, State, RenderModel> Formula<Input, State, Unit, RenderModel>.state(
    input: Observable<Input>
): Observable<RenderModel> {
    return state(
        input = input,
        onEvent = {}
    )
}

fun <Input, State, Output, RenderModel> Formula<Input, State, Output, RenderModel>.state(
    input: Observable<Input>,
    onEvent: (Output) -> Unit
): Observable<RenderModel> {
    return ProcessorFormulaRxRuntime.start(
        input = input,
        formula = this,
        onEvent = onEvent
    )
}

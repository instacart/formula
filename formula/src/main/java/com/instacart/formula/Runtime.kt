package com.instacart.formula

import io.reactivex.Observable

fun <Input, State, Effect, RenderModel> ProcessorFormula<Input, State, Effect, RenderModel>.state(
    input: Input,
    onEffect: (Effect) -> Unit = {}
): Observable<RenderModel> {
    return ProcessorFormulaRxRuntime.start(input, this, onEffect = onEffect)
}

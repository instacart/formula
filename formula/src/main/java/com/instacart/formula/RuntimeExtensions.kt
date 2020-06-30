@file:Suppress("DeprecatedCallableAddReplaceWith")

package com.instacart.formula

import io.reactivex.rxjava3.core.Observable

@Deprecated("Call Formula.start instead (remove thie com.instacart.formula.start import)")
fun <State, RenderModel : Any> Formula<Unit, State, RenderModel>.start(): Observable<RenderModel> {
    return start(input = Unit)
}

@Deprecated("Call Formula.start instead (remove thie com.instacart.formula.start import)")
fun <Input : Any, State, RenderModel : Any> Formula<Input, State, RenderModel>.start(
    input: Input
): Observable<RenderModel> {
    return start(input = Observable.just(input))
}

@Deprecated("Call Formula.start instead (remove thie com.instacart.formula.start import)")
fun <Input : Any, State, RenderModel : Any> Formula<Input, State, RenderModel>.start(
    input: Observable<Input>
): Observable<RenderModel> {
    return FormulaRuntime.start(input = input, formula = this)
}

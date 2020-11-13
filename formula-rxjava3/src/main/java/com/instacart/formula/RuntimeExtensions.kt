package com.instacart.formula

import com.instacart.formula.rxjava3.RxJavaRuntime
import com.instacart.formula.rxjava3.toObservable
import io.reactivex.rxjava3.core.Observable

@Deprecated("Moved to rxjava3 package", replaceWith = ReplaceWith(
    expression = "toObservable()",
    imports = arrayOf("com.instacart.formula.rxjava3.toObservable")
))
fun <Output : Any> IFormula<Unit, Output>.start(): Observable<Output> {
    return toObservable(input = Unit)
}

@Deprecated("Moved to rxjava3 package", replaceWith = ReplaceWith(
    expression = "toObservable(input)",
    imports = arrayOf("com.instacart.formula.rxjava3.toObservable")
))
fun <Input : Any, Output : Any> IFormula<Input, Output>.start(
    input: Input
): Observable<Output> {
    return toObservable(input = Observable.just(input))
}

@Deprecated("Moved to rxjava3 package", replaceWith = ReplaceWith(
    expression = "toObservable(input)",
    imports = arrayOf("com.instacart.formula.rxjava3.toObservable")
))
fun <Input : Any, Output : Any> IFormula<Input, Output>.start(
    input: Observable<Input>
): Observable<Output> {
    return RxJavaRuntime.start(input = input, formula = this)
}

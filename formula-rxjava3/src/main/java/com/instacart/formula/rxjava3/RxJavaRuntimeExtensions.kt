package com.instacart.formula.rxjava3

import com.instacart.formula.IFormula
import com.instacart.formula.Inspector
import io.reactivex.rxjava3.core.Observable

fun <Output : Any> IFormula<Unit, Output>.toObservable(
    inspector: Inspector? = null,
): Observable<Output> {
    return toObservable(input = Unit, inspector)
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toObservable(
    input: Input,
    inspector: Inspector? = null,
): Observable<Output> {
    return toObservable(input = Observable.just(input), inspector)
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toObservable(
    input: Observable<Input>,
    inspector: Inspector? = null,
    isValidationEnabled: Boolean = false,
): Observable<Output> {
    return RxJavaRuntime.start(
        input = input,
        formula = this,
        inspector = inspector,
        isValidationEnabled = isValidationEnabled,
    )
}

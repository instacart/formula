package com.instacart.formula.rxjava3

import com.instacart.formula.IFormula
import com.instacart.formula.RuntimeConfig
import com.instacart.formula.plugin.Inspector
import io.reactivex.rxjava3.core.Observable

fun <Output : Any> IFormula<Unit, Output>.toObservable(
    config: RuntimeConfig? = null,
): Observable<Output> {
    return toObservable(input = Unit, config)
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toObservable(
    input: Input,
    config: RuntimeConfig? = null,
): Observable<Output> {
    return toObservable(input = Observable.just(input), config)
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toObservable(
    input: Observable<Input>,
    config: RuntimeConfig? = null,
): Observable<Output> {
    return RxJavaRuntime.start(
        input = input,
        formula = this,
        config = config,
    )
}

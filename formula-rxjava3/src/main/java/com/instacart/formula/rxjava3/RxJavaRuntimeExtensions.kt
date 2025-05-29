package com.instacart.formula.rxjava3

import com.instacart.formula.IFormula
import com.instacart.formula.RuntimeConfig
import com.instacart.formula.toFlow
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.asObservable

fun <Output : Any> IFormula<Unit, Output>.toObservable(
    config: RuntimeConfig? = null,
): Observable<Output> {
    return toFlow(config).asObservable()
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toObservable(
    input: Input,
    config: RuntimeConfig? = null,
): Observable<Output> {
    return toFlow(input, config).asObservable()
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toObservable(
    input: Observable<Input>,
    config: RuntimeConfig? = null,
): Observable<Output> {
    val inputFlow = input.asFlow()
    return toFlow(inputFlow, config).asObservable()
}

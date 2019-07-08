package com.instacart.formula.rxjava3

import com.instacart.formula.IFormula
import com.instacart.formula.Logger
import io.reactivex.rxjava3.core.Observable

fun <Output : Any> IFormula<Unit, Output>.toObservable(logger: Logger? = null): Observable<Output> {
    return toObservable(
        input = Unit,
        logger = logger
    )
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toObservable(
    input: Input,
    logger: Logger? = null,
): Observable<Output> {
    return toObservable(
        input = Observable.just(input),
        logger = logger
    )
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toObservable(
    input: Observable<Input>,
    logger: Logger? = null,
): Observable<Output> {
    return RxJavaRuntime.start(
        input = input,
        formula = this,
        logger = logger,
    )
}

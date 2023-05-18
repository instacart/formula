package com.instacart.formula.rxjava3

import com.instacart.formula.IFormula
import com.instacart.formula.coroutines.toFlow
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.rx3.asObservable

object RxJavaRuntime {
    fun <Input : Any, Output : Any> start(
        input: Observable<Input>,
        formula: IFormula<Input, Output>
    ): Observable<Output> {
        val inputFlow = input.toFlowable(BackpressureStrategy.LATEST).asFlow()
        return formula.toFlow(inputFlow).asObservable()
    }
}
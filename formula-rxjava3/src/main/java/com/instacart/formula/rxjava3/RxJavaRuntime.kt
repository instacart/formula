package com.instacart.formula.rxjava3

import com.instacart.formula.FormulaRuntime
import com.instacart.formula.IFormula
import com.instacart.formula.Inspector
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.FormulaDisposableHelper

object RxJavaRuntime {
    fun <Input : Any, Output : Any> start(
        input: Observable<Input>,
        formula: IFormula<Input, Output>,
        inspector: Inspector? = null,
        isValidationEnabled: Boolean = false,
    ): Observable<Output> {
        return Observable.create<Output> { emitter ->
            val runtime = FormulaRuntime(
                formula = formula,
                onOutput = emitter::onNext,
                onError = emitter::onError,
                inspector = inspector,
                isValidationEnabled = isValidationEnabled,
            )

            val disposables = CompositeDisposable()
            disposables.add(input.subscribe(runtime::onInput, emitter::onError))
            disposables.add(FormulaDisposableHelper.fromRunnable(runtime::terminate))

            emitter.setDisposable(disposables)
        }.distinctUntilChanged()
    }
}
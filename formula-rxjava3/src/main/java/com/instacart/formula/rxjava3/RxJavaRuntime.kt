package com.instacart.formula.rxjava3

import com.instacart.formula.FormulaRuntime
import com.instacart.formula.IFormula
import com.instacart.formula.RuntimeConfig
import com.instacart.formula.plugin.Inspector
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.FormulaDisposableHelper

object RxJavaRuntime {
    fun <Input : Any, Output : Any> start(
        input: Observable<Input>,
        formula: IFormula<Input, Output>,
        config: RuntimeConfig?,
    ): Observable<Output> {
        return Observable.create<Output> { emitter ->
            val runtime = FormulaRuntime(
                formula = formula,
                onOutput = emitter::onNext,
                onError = emitter::onError,
                config = config ?: RuntimeConfig(),
            )

            val disposables = CompositeDisposable()
            disposables.add(input.subscribe(runtime::onInput, emitter::onError))
            disposables.add(FormulaDisposableHelper.fromRunnable(runtime::terminate))

            emitter.setDisposable(disposables)
        }.distinctUntilChanged()
    }
}
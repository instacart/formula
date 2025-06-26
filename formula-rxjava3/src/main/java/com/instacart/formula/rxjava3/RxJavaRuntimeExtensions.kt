package com.instacart.formula.rxjava3

import com.instacart.formula.FormulaRuntime
import com.instacart.formula.IFormula
import com.instacart.formula.RuntimeConfig
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

fun <Output : Any> IFormula<Unit, Output>.toObservable(
    config: RuntimeConfig? = null,
): Observable<Output> {
    return toObservable(Unit, config)
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toObservable(
    input: Input,
    config: RuntimeConfig? = null,
): Observable<Output> {
    return toObservable(Observable.just(input), config)
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toObservable(
    input: Observable<Input>,
    config: RuntimeConfig? = null,
): Observable<Output> {
    return start(this, input, config)
}

fun <Input : Any, Output : Any> start(
    formula: IFormula<Input, Output>,
    input: Observable<Input>,
    config: RuntimeConfig?,
): Observable<Output> {
    return Observable.create { emitter ->
        val runtime = FormulaRuntime(
            formula = formula,
            config = config ?: RuntimeConfig(),
        )
        runtime.setOnOutput(emitter::onNext)
        runtime.setOnError(emitter::onError)

        val disposables = CompositeDisposable()
        disposables.add(input.subscribe(runtime::onInput, emitter::onError))
        disposables.add(runtimeDisposable(runtime))
        emitter.setDisposable(disposables)
    }.distinctUntilChanged()
}

private fun runtimeDisposable(runtime: FormulaRuntime<*, *>): Disposable {
    return Disposable.fromRunnable(runtime::terminate)
}
package com.instacart.formula.rxjava3

import com.instacart.formula.FormulaRuntime
import com.instacart.formula.IFormula
import com.instacart.formula.internal.ThreadChecker
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.FormulaDisposableHelper

object RxJavaRuntime {
    fun <Input : Any, Output : Any> start(
        input: Observable<Input>,
        formula: IFormula<Input, Output>,
        isValidationEnabled: Boolean = false,
    ): Observable<Output> {
        val threadChecker = ThreadChecker(formula)
        return Observable.create<Output> { emitter ->
            val runtimeFactory = {
                FormulaRuntime(threadChecker, formula, emitter::onNext, emitter::onError, isValidationEnabled)
            }

            threadChecker.check("Need to subscribe on main thread.")

            var runtime = runtimeFactory()

            val disposables = CompositeDisposable()
            disposables.add(input.subscribe({ input ->
                threadChecker.check("Input arrived on a wrong thread.")
                if (!runtime.isKeyValid(input)) {
                    runtime.terminate()
                    runtime = runtimeFactory()
                }
                runtime.onInput(input)
            }, emitter::onError))

            val runnable = Runnable {
                threadChecker.check("Need to unsubscribe on the main thread.")
                runtime.terminate()
            }
            disposables.add(FormulaDisposableHelper.fromRunnable(runnable))

            emitter.setDisposable(disposables)
        }.distinctUntilChanged()
    }
}
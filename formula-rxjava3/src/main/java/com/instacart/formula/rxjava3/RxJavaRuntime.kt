package com.instacart.formula.rxjava3

import com.instacart.formula.FormulaRuntime
import com.instacart.formula.IFormula
import com.instacart.formula.Logger
import com.instacart.formula.internal.FormulaLogger
import com.instacart.formula.internal.ThreadChecker
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.FormulaDisposableHelper

object RxJavaRuntime {
    fun <Input : Any, Output : Any> start(
        input: Observable<Input>,
        formula: IFormula<Input, Output>,
        logger: Logger? = null,
    ): Observable<Output> {
        val threadChecker = ThreadChecker()
        return Observable.create<Output> { emitter ->
            threadChecker.check("Need to subscribe on main thread.")
            var runtime = FormulaRuntime(threadChecker, formula, logger, emitter::onNext, emitter::onError)
            val disposables = CompositeDisposable()
            disposables.add(input.subscribe({ input ->
                threadChecker.check("Input arrived on a wrong thread.")
                if (!runtime.isKeyValid(input)) {
                    runtime.terminate()
                    runtime = FormulaRuntime(
                        threadChecker = threadChecker,
                        formula = formula,
                        loggerDelegate = logger,
                        onOutput = emitter::onNext,
                        onError = emitter::onError
                    )
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
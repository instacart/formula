package com.instacart.formula.rxjava3

import com.instacart.formula.FormulaPlugins
import com.instacart.formula.FormulaRuntime
import com.instacart.formula.IFormula
import com.instacart.formula.Inspector
import com.instacart.formula.internal.ThreadChecker
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.FormulaDisposableHelper

object RxJavaRuntime {

    private var defaultErrorHandler: RxJavaRuntimeErrorHandler? = null

    fun setDefaultErrorHandler(errorHandler: RxJavaRuntimeErrorHandler?) {
        this.defaultErrorHandler = errorHandler
    }

    fun <Input : Any, Output : Any> start(
        input: Observable<Input>,
        formula: IFormula<Input, Output>,
        inspector: Inspector? = null,
        isValidationEnabled: Boolean = false,
    ): Observable<Output> {
        val threadChecker = ThreadChecker(formula)
        return Observable.create<Output> { emitter ->
            val mergedInspector = FormulaPlugins.inspector(
                type = formula.type(),
                local = inspector,
            )

            val onError = { error: Throwable ->
                val handled = defaultErrorHandler?.onError(error) ?: false
                if (!handled) emitter.onError(error)
            }.takeIf { defaultErrorHandler != null } ?: emitter::onError

            val runtimeFactory = {
                FormulaRuntime(
                    threadChecker = threadChecker,
                    formula = formula,
                    onOutput = emitter::onNext,
                    onError = onError,
                    inspector = mergedInspector,
                    isValidationEnabled = isValidationEnabled,
                )
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
            }, onError))

            val runnable = Runnable {
                threadChecker.check("Need to unsubscribe on the main thread.")
                runtime.terminate()
            }
            disposables.add(FormulaDisposableHelper.fromRunnable(runnable))

            emitter.setDisposable(disposables)
        }.distinctUntilChanged()
    }
}

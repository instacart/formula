package com.instacart.formula.rxjava3

import com.instacart.formula.FormulaRuntime
import com.instacart.formula.IFormula
import com.instacart.formula.internal.ThreadChecker
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.FormulaDisposableHelper
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.Executor

object RxJavaRuntime {
    fun <Input : Any, Output : Any> start(
        input: Observable<Input>,
        formula: IFormula<Input, Output>,
        executor: Executor,
    ): Observable<Output> {
        val threadChecker = ThreadChecker(formula)
        return Observable.create<Output> { emitter ->
            threadChecker.check("Need to subscribe on main thread.")

            var runtime = FormulaRuntime(threadChecker, formula, emitter::onNext, emitter::onError, executor)

            val disposables = CompositeDisposable()
            disposables.add(input
                .observeOn(Schedulers.from(executor))
                .subscribe({ input ->
                threadChecker.check("Input arrived on a wrong thread.")
                if (!runtime.isKeyValid(input)) {
                    runtime.terminate()
                    runtime = FormulaRuntime(threadChecker, formula, emitter::onNext, emitter::onError, executor)
                }
                runtime.onInput(input)
            }, emitter::onError))

            val runnable = Runnable {
                executor.execute {

                }
                threadChecker.check("Need to unsubscribe on the main thread.")
                runtime.terminate()
            }
            disposables.add(FormulaDisposableHelper.fromRunnable(runnable))

            emitter.setDisposable(disposables)
        }.distinctUntilChanged()
    }
}
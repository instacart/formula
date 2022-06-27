package com.instacart.formula.test

import com.instacart.formula.IFormula
import com.instacart.formula.rxjava3.toObservable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Binds [formula] to RxJava3 runtime.
 */
class RxJavaFormulaTestDelegate<Input : Any, Output : Any, FormulaT : IFormula<Input, Output>>(
    override val formula: FormulaT
) : FormulaTestDelegate<Input, Output, FormulaT> {
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private val inputRelay = BehaviorSubject.create<Input>()
    private val observer = formula
        .toObservable(inputRelay, executor = executor)
        .test()

    override fun values(): List<Output> {
        ensureFormulaIsIdle()
        return observer.values()
    }

    override fun assertNoErrors() {
        ensureFormulaIsIdle()
        observer.assertNoErrors()
    }

    override fun dispose() {
        ensureFormulaIsIdle()
        observer.dispose()
        ensureFormulaIsIdle()
    }

    override fun input(input: Input) {
        ensureFormulaIsIdle()
        inputRelay.onNext(input)
//        ensureFormulaIsIdle()
    }

    private fun ensureFormulaIsIdle() {
        val latch = CountDownLatch(1)
        executor.execute { latch.countDown() }
        latch.await(100, TimeUnit.MILLISECONDS)
    }
}
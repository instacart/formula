package com.instacart.formula.test

import com.instacart.formula.IFormula
import com.instacart.formula.rxjava3.toObservable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class TestFormulaObserver<Input : Any, Output : Any, FormulaT : IFormula<Input, Output>>(
    val formula: FormulaT
) {

    private var started: Boolean = false
    private val inputRelay = BehaviorSubject.create<Input>()
    private val observer = formula
        .toObservable(inputRelay)
        .test()
        .assertNoErrors()

    fun values(): List<Output> {
        return observer.values()
    }

    /**
     * Passes input to [formula].
     */
    fun input(value: Input) = apply {
        started = true
        inputRelay.onNext(value)
        assertNoErrors()
    }

    inline fun output(assert: Output.() -> Unit) = apply {
        ensureFormulaIsRunning()
        assert(values().last())
        assertNoErrors()
    }

    fun assertOutputCount(count: Int) = apply {
        ensureFormulaIsRunning()
        val size = values().size
        assert(size == count) {
            "Expected: $count, was: $size"
        }
    }

    fun assertNoErrors() = apply {
        observer.assertNoErrors()
    }

    fun dispose() = apply {
        observer.dispose()
    }

    @PublishedApi
    internal fun ensureFormulaIsRunning() {
        if (!started) throw IllegalStateException("Formula is not running. Call [TeatFormulaObserver.input] to start it.")
    }
}

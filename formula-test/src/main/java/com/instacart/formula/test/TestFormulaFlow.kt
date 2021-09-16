package com.instacart.formula.test

import com.instacart.formula.IFormula
import com.instacart.formula.coroutines.toFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class TestFormulaFlow<Input : Any, Output : Any, FormulaT : IFormula<Input, Output>>(
    scope: CoroutineScope,
    val formula: FormulaT
) {

    private var started: Boolean = false
    private val inputFlow =
        MutableSharedFlow<Input>(1)
    private val testFlow = formula
        .toFlow(inputFlow.distinctUntilChanged())
        .test(scope)
        .assertNoErrors()

    fun values(): List<Output> {
        return testFlow.values()
    }

    /**
     * Passes input to [formula].
     */
    fun input(value: Input) = apply {
        started = true
        assertNoErrors() // Check before interaction
        inputFlow.tryEmit(value)
        assertNoErrors() // Check after interaction
    }

    inline fun output(assert: Output.() -> Unit) = apply {
        ensureFormulaIsRunning()
        assertNoErrors() // Check before interaction
        assert(values().last())
        assertNoErrors() // Check after interaction
    }

    fun assertOutputCount(count: Int) = apply {
        ensureFormulaIsRunning()
        assertNoErrors()
        val size = values().size
        assert(size == count) {
            "Expected: $count, was: $size"
        }
    }

    fun assertNoErrors() = apply {
        testFlow.assertNoErrors()
    }

    @PublishedApi
    internal fun ensureFormulaIsRunning() {
        if (!started) throw IllegalStateException("Formula is not running. Call [TeatFormulaObserver.input] to start it.")
    }
}

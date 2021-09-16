package com.instacart.formula.test

import com.instacart.formula.IFormula

class TestFormulaObserver<Input : Any, Output : Any, FormulaT : IFormula<Input, Output>>(
    private val delegate: FormulaTestDelegate<Input, Output, FormulaT>,
) {

    private var started: Boolean = false

    val formula: FormulaT = delegate.formula

    init {
        delegate.assertNoErrors()
    }

    fun values(): List<Output> {
        return delegate.values()
    }

    /**
     * Passes input to [formula].
     */
    fun input(value: Input) = apply {
        started = true
        assertNoErrors() // Check before interaction
        delegate.input(value)
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
        delegate.assertNoErrors()
    }

    fun dispose() = apply {
        delegate.dispose()
    }

    @PublishedApi
    internal fun ensureFormulaIsRunning() {
        if (!started) throw IllegalStateException("Formula is not running. Call [TestFormulaObserver.input] to start it.")
    }
}

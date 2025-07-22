package com.instacart.formula.test

import com.instacart.formula.IFormula
import com.instacart.formula.RuntimeConfig
import com.instacart.formula.start
import com.instacart.formula.toFlow
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class TestFormulaObserver<Input : Any, Output : Any, FormulaT : IFormula<Input, Output>>(
    private val runtimeConfig: RuntimeConfig,
    val formula: FormulaT,
    private val isValidationEnabled: Boolean = true,
) {

    private val values = mutableListOf<Output>()
    private val errors = mutableListOf<Throwable>()

    private val inputFlow = MutableSharedFlow<Input>(
        replay = 1,
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val job = GlobalScope.launch(
        context = Dispatchers.Unconfined,
        start = CoroutineStart.UNDISPATCHED,
    ) {
        val flow = start(inputFlow, formula, runtimeConfig, isValidationEnabled)
        flow
            .catch { errors.add(it) }
            .collect { values.add(it) }
    }

    private var started: Boolean = false

    init {
        assertNoErrors()
    }

    fun values(): List<Output> {
        return values.toList()
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

    fun output(assert: Output.() -> Unit) = apply {
        ensureFormulaIsRunning()
        assertNoErrors() // Check before interaction
        assert(values().last())
        assertNoErrors() // Check after interaction
    }

    fun assertOutputCount(count: Int) = apply {
        ensureFormulaIsRunning()
        assertNoErrors()
        val size = values().size

        if (size != count) {
            throw AssertionError("Expected: $count, was: $size")
        }
    }

    fun assertNoErrors() = apply {
        val error = errors.lastOrNull()
        if (error != null) {
            throw error
        }
    }

    fun dispose() = apply {
        job.cancel()
    }

    @PublishedApi
    internal fun ensureFormulaIsRunning() {
        if (!started) throw IllegalStateException("Formula is not running. Call [TestFormulaObserver.input] to start it.")
    }
}

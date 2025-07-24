package com.instacart.formula.test

import com.instacart.formula.IFormula
import com.instacart.formula.RuntimeConfig
import com.instacart.formula.plugin.Dispatcher
import com.instacart.formula.plugin.Inspector
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
    private val failOnError: Boolean,
    isValidationEnabled: Boolean,
    dispatcher: Dispatcher?,
    inspector: Inspector?,
    val formula: FormulaT,
) {
    private val values = mutableListOf<Output>()
    private val errors = mutableListOf<Throwable>()

    private val inputFlow = MutableSharedFlow<Input>(
        replay = 1,
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val runtimeConfig = RuntimeConfig(
        isValidationEnabled = isValidationEnabled,
        inspector = inspector,
        defaultDispatcher = dispatcher,
        onError = { errors.add(it.error) }
    )

    private val job = GlobalScope.launch(
        context = Dispatchers.Unconfined,
        start = CoroutineStart.UNDISPATCHED,
    ) {
        formula.toFlow(inputFlow, runtimeConfig)
            .catch { errors.add(it) }
            .collect { values.add(it) }
    }

    private var started: Boolean = false

    init {
        failOnError()
    }

    fun errors(): List<Throwable> {
        return errors.toList()
    }

    fun values(): List<Output> {
        return values.toList()
    }

    /**
     * Passes input to [formula].
     */
    fun input(value: Input) = apply {
        started = true

        failOnError() // Check before interaction
        inputFlow.tryEmit(value)
        failOnError() // Check after interaction
    }

    fun output(assert: Output.() -> Unit) = apply {
        ensureFormulaIsRunning()
        failOnError() // Check before interaction
        assert(values().last())
        failOnError() // Check after interaction
    }

    fun assertOutputCount(count: Int) = apply {
        ensureFormulaIsRunning()
        failOnError()
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

    fun assertHasErrors() = apply {
        if (errors.isEmpty()) {
            throw AssertionError("There were no errors")
        }
    }

    fun dispose() = apply {
        job.cancel()
    }

    /**
     * Provides automatic error checking.
     */
    private fun failOnError() {
        if (failOnError) {
            assertNoErrors()
        }
    }

    @PublishedApi
    internal fun ensureFormulaIsRunning() {
        if (!started) throw IllegalStateException("Formula is not running. Call [TestFormulaObserver.input] to start it.")
    }
}

package com.instacart.formula.test

import com.instacart.formula.IFormula
import com.instacart.formula.RuntimeConfig
import com.instacart.formula.plugin.Dispatcher
import com.instacart.formula.plugin.Inspector
import com.instacart.formula.toFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.coroutines.CoroutineContext

class TestFormulaObserver<Input : Any, Output : Any, FormulaT : IFormula<Input, Output>>(
    parentContext: CoroutineContext,
    private val failOnError: Boolean,
    isValidationEnabled: Boolean,
    dispatcher: Dispatcher?,
    inspector: Inspector?,
    val formula: FormulaT,
) {
    // Use parent scheduler or create own.
    val coroutineScheduler = parentContext[TestCoroutineScheduler] ?: TestCoroutineScheduler()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val scope = CoroutineScope(UnconfinedTestDispatcher(coroutineScheduler) + SupervisorJob()).apply {
        // Make sure to cancel our scope when parent job is cancelled.
        parentContext[Job]?.invokeOnCompletion { this.cancel() }
    }

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

    private var started: Boolean = false

    init {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            formula.toFlow(inputFlow, runtimeConfig)
                .catch { errors.add(it) }
                .collect { values.add(it) }
        }

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
        scope.cancel()
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

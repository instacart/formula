package com.instacart.formula

import com.instacart.formula.plugin.Dispatcher
import com.instacart.formula.plugin.FormulaError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Runs input-less formula as a [StateFlow].
 *
 * @param scope Formula will be launched using this coroutine scope.
 */
fun <Output : Any> IFormula<Unit, Output>.runAsStateFlow(
    scope: CoroutineScope,
): StateFlow<Output> {
    return runAsStateFlow(scope, Unit)
}

/**
 * Runs formula as a [StateFlow].
 *
 * @param scope Formula will be launched using this coroutine scope.
 * @param input Input used to start the formula
 */
fun <Input : Any, Output : Any> IFormula<Input, Output>.runAsStateFlow(
    scope: CoroutineScope,
    input: Input,
): StateFlow<Output> {
    // This will throw an exception if [scope] does not contain a job
    val job = scope.coroutineContext.job

    // Since runAsStateFlow has to execute on the current thread,
    // having a specific dispatcher does not make sense.
    val config = RuntimeConfig(defaultDispatcher = Dispatcher.None)
    val runtime = asRuntime(scope.coroutineContext, config)
    runtime.setOnError { e ->
        val error = FormulaError.Unhandled(type().java, e)
        FormulaPlugins.onError(error)
    }

    // Setup cancellation
    job.invokeOnCompletion { runtime.terminate() }

    runtime.onInput(input)

    // Listen and emit updates
    val stateFlow = MutableStateFlow(runtime.requireOutput())
    runtime.setOnOutput(stateFlow::tryEmit)
    return stateFlow
}

fun <Output : Any> IFormula<Unit, Output>.toFlow(
    config: RuntimeConfig? = null,
): Flow<Output> {
    return toFlow(input = Unit, config)
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toFlow(
    input: Input,
    config: RuntimeConfig? = null,
): Flow<Output> {
    return toFlow(input = flowOf(input), config)
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toFlow(
    input: Flow<Input>,
    config: RuntimeConfig? = null,
): Flow<Output> {
    return start(
        input = input,
        formula = this,
        config = config,
    )
}

private fun <Input : Any, Output : Any> start(
    input: Flow<Input>,
    formula: IFormula<Input, Output>,
    config: RuntimeConfig?,
): Flow<Output> {
    val callbackFlow = callbackFlow {
        val runtime = formula.asRuntime(coroutineContext, config)
        runtime.setOnOutput(this::trySend)
        runtime.setOnError(this::close)

        launch {
            input.collect(runtime::onInput)
        }

        awaitClose {
            runtime.terminate()
        }
    }
    return callbackFlow
        .conflate()
        .distinctUntilChanged()
}


private fun <Input : Any, Output : Any> IFormula<Input, Output>.asRuntime(
    coroutineContext: CoroutineContext,
    config: RuntimeConfig?,
): FormulaRuntime<Input, Output> {
    return FormulaRuntime(
        coroutineContext = coroutineContext,
        config = config ?: RuntimeConfig(),
        formula = this,
    )
}
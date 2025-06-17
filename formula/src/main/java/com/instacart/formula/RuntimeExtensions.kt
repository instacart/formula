package com.instacart.formula

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

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
        val runtime = FormulaRuntime(
            coroutineContext = coroutineContext,
            formula = formula,
            onOutput = this::trySend,
            onError = this::close,
            config = config ?: RuntimeConfig(),
        )

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

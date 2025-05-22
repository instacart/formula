package com.instacart.formula.coroutines

import com.instacart.formula.FormulaRuntime
import com.instacart.formula.IFormula
import com.instacart.formula.RuntimeConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

object FlowRuntime {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun <Input : Any, Output : Any> start(
        input: Flow<Input>,
        formula: IFormula<Input, Output>,
        config: RuntimeConfig?,
    ): Flow<Output> {
        val callbackFlow = callbackFlow {
            val runtime = FormulaRuntime(
                formula = formula,
                onOutput = this::trySendBlocking,
                onError = this::close,
                config = config ?: RuntimeConfig(),
            )

            input.onEach(runtime::onInput).launchIn(this)

            awaitClose {
                runtime.terminate()
            }
        }
        return callbackFlow.distinctUntilChanged()
    }
}
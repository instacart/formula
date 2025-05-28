package com.instacart.formula.coroutines

import com.instacart.formula.FormulaRuntime
import com.instacart.formula.IFormula
import com.instacart.formula.RuntimeConfig
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

object FlowRuntime {

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

            val inputJob = applyInputs(runtime, input)

            awaitClose {
                inputJob.cancel()
                runtime.terminate()
            }
        }
        return callbackFlow.distinctUntilChanged()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun <Input : Any> applyInputs(
        runtime: FormulaRuntime<Input, *>,
        input: Flow<Input>
    ): Job {
        return GlobalScope.launch(
            context = Dispatchers.Unconfined,
            start = CoroutineStart.UNDISPATCHED,
        ) {
            input.collect(runtime::onInput)
        }
    }
}
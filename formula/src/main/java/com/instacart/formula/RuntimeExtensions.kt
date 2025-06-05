package com.instacart.formula

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.produceIn
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
        inputFlow = input,
        formula = this,
        config = config,
    )
}

private fun <Input : Any, Output : Any> start(
    inputFlow: Flow<Input>,
    formula: IFormula<Input, Output>,
    config: RuntimeConfig?,
): Flow<Output> {
    val flow = flow<Output> {
        val channel = Channel<Output>(capacity = Channel.CONFLATED)
        coroutineScope {
            val scope = this
            val runtime = FormulaRuntime(
                scope = scope,
                formula = formula,
                onOutput = channel::trySend,
                onError = channel::close,
                config = config ?: RuntimeConfig(),
            )

            // Input producer scope launch can be problematic?
            // TODO: launching input
            launch {
//                val inputChannel = input.produceIn(this)
//                while (true) {
//                    val input = inputChannel.receive()
//                    runtime.onInput()
//
//                }

                inputFlow.conflate().collect {
                    runtime.onInput(it)
                }
            }

            try {
                while (true) {
                    val output = channel.receive()
                    emit(output)
                }
            } finally {
                runtime.terminate()
            }
        }
    }
    return flow.distinctUntilChanged()


//    val callbackFlow = callbackFlow {
//        val runtime = FormulaRuntime(
//            scope = this,
//            formula = formula,
//            onOutput = this::trySend,
//            onError = this::close,
//            config = config ?: RuntimeConfig(),
//        )
//
//        launch(context = Dispatchers.Unconfined, start = CoroutineStart.UNDISPATCHED) {
//            input.collect(runtime::onInput)
//        }
//
//        awaitClose {
//            runtime.terminate()
//        }
//    }
//    return callbackFlow
//        .buffer(onBufferOverflow = BufferOverflow.DROP_OLDEST)
//        .distinctUntilChanged()
}

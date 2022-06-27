package com.instacart.formula.coroutines

import com.instacart.formula.IFormula
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.concurrent.Executor
import java.util.concurrent.Executors

fun <Output : Any> IFormula<Unit, Output>.toFlow(): Flow<Output> {
    return toFlow(input = Unit)
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toFlow(
    input: Input
): Flow<Output> {
    return toFlow(input = flowOf(input))
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toFlow(
    input: Flow<Input>,
    executor: Executor = Executors.newSingleThreadExecutor(),
): Flow<Output> {
    return FlowRuntime.start(input = input, formula = this, executor = executor)
}

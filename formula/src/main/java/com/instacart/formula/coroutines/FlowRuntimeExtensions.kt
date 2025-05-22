package com.instacart.formula.coroutines

import com.instacart.formula.IFormula
import com.instacart.formula.RuntimeConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

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
    return FlowRuntime.start(
        input = input,
        formula = this,
        config = config,
    )
}

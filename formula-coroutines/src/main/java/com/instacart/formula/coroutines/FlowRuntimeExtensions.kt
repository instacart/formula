package com.instacart.formula.coroutines

import com.instacart.formula.IFormula
import com.instacart.formula.Inspector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

fun <Output : Any> IFormula<Unit, Output>.toFlow(
    inspector: Inspector? = null,
): Flow<Output> {
    return toFlow(input = Unit, inspector)
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toFlow(
    input: Input,
    inspector: Inspector? = null,
): Flow<Output> {
    return toFlow(input = flowOf(input), inspector)
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toFlow(
    input: Flow<Input>,
    inspector: Inspector? = null,
    isValidationEnabled: Boolean = false,
): Flow<Output> {
    return FlowRuntime.start(
        input = input,
        formula = this,
        inspector = inspector,
        isValidationEnabled = isValidationEnabled,
    )
}

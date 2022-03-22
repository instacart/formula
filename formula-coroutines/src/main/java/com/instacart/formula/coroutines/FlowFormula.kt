package com.instacart.formula.coroutines

import com.instacart.formula.Action
import com.instacart.formula.ActionFormula
import kotlinx.coroutines.flow.Flow

abstract class FlowFormula<Input : Any, Output : Any> : ActionFormula<Input, Output>() {

    abstract override fun initialValue(input: Input): Output

    abstract fun flow(input: Input): Flow<Output>

    final override fun action(input: Input): Action<Output> {
        return FlowAction.fromFlow {
            flow(input)
        }
    }
}
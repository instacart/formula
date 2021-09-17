package com.instacart.formula.coroutines

import com.instacart.formula.Stream
import com.instacart.formula.StreamFormula
import kotlinx.coroutines.flow.Flow

abstract class FlowFormula<Input : Any, Output : Any> : StreamFormula<Input, Output>() {

    abstract override fun initialValue(input: Input): Output

    abstract fun flow(input: Input): Flow<Output>

    final override fun stream(input: Input): Stream<Output> {
        return FlowStream.fromFlow {
            flow(input)
        }
    }
}
package com.instacart.formula.counter

import com.instacart.formula.FormulaContext
import com.instacart.formula.ProcessResult
import com.instacart.formula.ProcessorFormula

class CounterProcessorFormula : ProcessorFormula<Unit, Int, Unit, CounterRenderModel> {

    override fun initialState(input: Unit): Int = 0

    override fun process(
        input: Unit,
        state: Int,
        context: FormulaContext<Int, Unit>
    ): ProcessResult<CounterRenderModel> {
        return ProcessResult(
            renderModel = CounterRenderModel(
                count = "Count: $state",
                onDecrement = {
                    context.transition(state - 1)
                },
                onIncrement = {
                    context.transition(state + 1)
                }
            )
        )
    }
}

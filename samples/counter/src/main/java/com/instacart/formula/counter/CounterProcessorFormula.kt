package com.instacart.formula.counter

import com.instacart.formula.FormulaContext
import com.instacart.formula.Evaluation
import com.instacart.formula.ProcessorFormula

class CounterProcessorFormula : ProcessorFormula<Unit, Int, Unit, CounterRenderModel> {

    override fun initialState(input: Unit): Int = 0

    override fun evaluate(
        input: Unit,
        state: Int,
        context: FormulaContext<Int, Unit>
    ): Evaluation<CounterRenderModel> {
        return Evaluation(
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

package com.instacart.formula.counter

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext

class CounterFormula : Formula<Unit, Int, CounterRenderModel> {

    override fun initialState(input: Unit): Int = 0

    override fun evaluate(
        input: Unit,
        state: Int,
        context: FormulaContext<Int>
    ): Evaluation<CounterRenderModel> {
        return Evaluation(
            output = CounterRenderModel(
                count = "Count: $state",
                onDecrement = context.onEvent {
                    transition(state - 1)
                },
                onIncrement = context.onEvent {
                    transition(state + 1)
                }
            )
        )
    }
}

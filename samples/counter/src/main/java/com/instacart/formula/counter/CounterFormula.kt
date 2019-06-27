package com.instacart.formula.counter

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext

class CounterFormula : Formula<Unit, Int, Unit, CounterRenderModel> {

    override fun initialState(input: Unit): Int = 0

    override fun evaluate(
        input: Unit,
        state: Int,
        context: FormulaContext<Int, Unit>
    ): Evaluation<CounterRenderModel> {
        return Evaluation(
            renderModel = CounterRenderModel(
                count = "Count: $state",
                onDecrement = context.callback {
                    transition(state - 1)
                },
                onIncrement = context.callback {
                    transition(state + 1)
                }
            )
        )
    }
}

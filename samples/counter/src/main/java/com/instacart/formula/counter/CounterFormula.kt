package com.instacart.formula.counter

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot

class CounterFormula : Formula<Unit, Int, CounterRenderModel>() {

    override fun initialState(input: Unit): Int = 0

    override fun Snapshot<Unit, Int>.evaluate(): Evaluation<CounterRenderModel> {
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

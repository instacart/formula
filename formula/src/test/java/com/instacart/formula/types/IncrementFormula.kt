package com.instacart.formula.types

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot

/**
 * Formula with an output that contains current integer state and an event listener that
 * increments the integer state.
 */
class IncrementFormula: Formula<Unit, Int, IncrementFormula.Output>() {

    data class Output(
        val value: Int,
        val onIncrement: () -> Unit,
    )

    override fun initialState(input: Unit): Int {
        return 0
    }

    override fun Snapshot<Unit, Int>.evaluate(): Evaluation<Output> {
        return Evaluation(
            output = Output(
                value = state,
                onIncrement = context.callback {
                    transition(state + 1)
                },
            )
        )
    }
}
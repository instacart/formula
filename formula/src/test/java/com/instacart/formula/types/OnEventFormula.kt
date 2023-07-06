package com.instacart.formula.types

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot

class OnEventFormula<Input>(
    private val eventDelegate: Snapshot<Input, Int>.() -> (() -> Unit),
) : Formula<Input, Int, OnEventFormula.Output>() {

    data class Output(
        val state: Int,
        val onEvent: () -> Unit,
    )

    override fun initialState(input: Input): Int = 0

    override fun Snapshot<Input, Int>.evaluate(): Evaluation<Output> {
        return Evaluation(
            output = Output(
                state = state,
                onEvent = eventDelegate()
            )
        )
    }
}
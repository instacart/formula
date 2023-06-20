package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot

class SideEffectFormula(
    private val onSideEffect: () -> Unit
) : Formula<Unit, Int, SideEffectFormula.Output>() {

    data class Output(
        val triggerSideEffect: Listener<Unit>
    )

    override fun initialState(input: Unit): Int = 0

    override fun Snapshot<Unit, Int>.evaluate(): Evaluation<Output> {
        return Evaluation(
            output = Output(
                triggerSideEffect = context.onEvent {
                    transition { onSideEffect() }
                }
            )
        )
    }
}

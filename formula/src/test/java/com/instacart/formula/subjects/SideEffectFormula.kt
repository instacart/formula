package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.Listener

class SideEffectFormula(
    private val onSideEffect: () -> Unit
) : Formula<Unit, Int, SideEffectFormula.Output>() {

    class Output(
        val triggerSideEffect: Listener<Unit>
    )

    override fun initialState(input: Unit): Int = 0

    override fun evaluate(input: Unit, state: Int, context: FormulaContext<Int>): Evaluation<Output> {
        return Evaluation(
            output = Output(
                triggerSideEffect = context.onEvent {
                    transition { onSideEffect() }
                }
            )
        )
    }
}

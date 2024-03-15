package com.instacart.formula.types

import com.instacart.formula.ActionBuilder
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot

/**
 * Allows parent formula to pass a [DelegateAction].
 */
class ActionDelegateFormula : Formula<ActionDelegateFormula.Input, Int, Int>() {

    /**
     * Allows us to define a custom action.
     */
    fun interface DelegateAction {
        fun ActionBuilder<Input, Int>.runAction()
    }

    data class Input(
        val delegateAction: DelegateAction?,
        val onAction: () -> Unit
    )

    override fun initialState(input: Input): Int = 0

    override fun Snapshot<Input, Int>.evaluate(): Evaluation<Int> {
        return Evaluation(
            output = state,
            actions = context.actions {
                input.delegateAction?.apply {
                    runAction()
                }
            }
        )
    }
}
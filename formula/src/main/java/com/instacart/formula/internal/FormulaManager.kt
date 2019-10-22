package com.instacart.formula.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Effects

interface FormulaManager<Input, State, RenderModel> {

    fun setTransitionListener(listener: (Effects?, isValid: Boolean) -> Unit)

    fun updateTransitionNumber(number: Long)

    /**
     * Creates the current [RenderModel] and prepares the next frame that will need to be processed.
     */
    fun evaluate(
        formula: Formula<Input, State, RenderModel>,
        input: Input,
        transitionId: Long
    ): Evaluation<RenderModel>

    fun terminateOldUpdates(currentTransition: Long): Boolean

    fun startNewUpdates(currentTransition: Long): Boolean

    fun markAsTerminated()
}

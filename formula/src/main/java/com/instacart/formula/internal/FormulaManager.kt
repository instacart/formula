package com.instacart.formula.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula

interface FormulaManager<Input, State, Output, RenderModel> {

    fun setTransitionListener(listener: (Output?) -> Unit)

    /**
     * Creates the current [RenderModel] and prepares the next frame that will need to be processed.
     */
    fun evaluate(
        formula: Formula<Input, State, Output, RenderModel>,
        input: Input,
        currentTransition: Long
    ): Evaluation<RenderModel>


    fun terminateOldUpdates(currentTransition: Long): Boolean

    fun startNewUpdates(currentTransition: Long): Boolean

    fun processSideEffects(currentTransition: Long): Boolean

    fun markAsTerminated()

    fun clearSideEffects()
}

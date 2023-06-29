package com.instacart.formula.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula

interface FormulaManager<Input, Output> {
    /**
     * Sets validation mode before running an identical evaluation to verify that
     * inputs and outputs do not change.
     */
    fun setValidationRun(isValidationEnabled: Boolean)

    /**
     * Creates the current [Output] and prepares the next frame that will need to be processed.
     */
    fun evaluate(input: Input): Evaluation<Output>

    /**
     * Called after [evaluate] to terminate children that were removed.
     *
     * @return True if transition happened while performing this.
     */
    fun terminateDetachedChildren(): Boolean

    /**
     * Called after [evaluate] to terminate old streams.
     */
    fun terminateOldUpdates(): Boolean

    /**
     * Called after [evaluate] to start new streams.
     */
    fun startNewUpdates(): Boolean

    /**
     * Called when [Formula] is removed. This is should not trigger any external side-effects,
     * only mark itself and its children as terminated.
     */
    fun markAsTerminated()

    /**
     * Called when we are ready to perform termination side-effects.
     */
    fun performTerminationSideEffects()
}

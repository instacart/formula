package com.instacart.formula.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula

interface FormulaManager<Input, Output> {
    fun nextFrame(currentTransition: Long): Boolean

    fun updateTransitionNumber(number: Long)

    /**
     * Creates the current [Output] and prepares the next frame that will need to be processed.
     */
    fun evaluate(
        input: Input,
        transitionId: Long
    ): Evaluation<Output>

    /**
     * Called after [evaluate] to terminate children that were removed.
     *
     * @return True if transition happened while performing this.
     */
    fun terminateDetachedChildren(currentTransition: Long): Boolean

    /**
     * Called after [evaluate] to terminate old streams.
     */
    fun terminateOldUpdates(currentTransition: Long): Boolean

    /**
     * Called after [evaluate] to start new streams.
     */
    fun startNewUpdates(currentTransition: Long): Boolean

    /**
     * Called when [Formula] is removed. This is should not trigger any external side-effects,
     * only mark itself and its children as terminated.
     */
    fun markAsTerminated()

    /**
     * Called when we are ready to perform termination side-effects.
     */
    fun performTerminationSideEffects()

    fun terminate()
}

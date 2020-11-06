package com.instacart.formula.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula

interface FormulaManager<Input, Output> {
    /**
     * Creates the current [Output] and prepares the next frame that will need to be processed.
     */
    fun evaluate(
        input: Input,
        transitionId: TransitionId
    ): Evaluation<Output>

    /**
     * Method updates the transition id of the existing evaluation. This
     * method is called when there were no changes and evaluation does
     * not need to run in this part of the formula tree.
     */
    fun updateTransitionId(transitionId: TransitionId)

    /**
     * Called after [evaluate] to terminate children that were removed.
     *
     * @return True if transition happened while performing this.
     */
    fun terminateDetachedChildren(transitionId: TransitionId): Boolean

    /**
     * Called after [evaluate] to terminate old streams.
     */
    fun terminateOldUpdates(transitionId: TransitionId): Boolean

    /**
     * Called after [evaluate] to start new streams.
     */
    fun startNewUpdates(transitionId: TransitionId): Boolean

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

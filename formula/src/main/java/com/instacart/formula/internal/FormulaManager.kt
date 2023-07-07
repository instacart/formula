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
     * Calls [Formula.evaluate] and after evaluation, it will process the new state
     * by cleaning up detached child formulas, terminating old actions, and then starting
     * new ones. If at any given point there is a state change, it will run [Formula.evaluate].
     *
     * It will handle formula state changes internally and return the last [Output].
     */
    fun run(input: Input): Evaluation<Output>

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

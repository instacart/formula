package com.instacart.formula.runtime

import com.instacart.formula.Formula

internal interface FormulaManager<Input, Output> {
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
    fun run(input: Input): Output

    /**
     * Returns the last output produced by the formula or null.
     */
    fun lastOutput(): Output?

    fun isTerminated(): Boolean
}

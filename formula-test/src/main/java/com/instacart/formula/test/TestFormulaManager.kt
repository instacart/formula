package com.instacart.formula.test

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.internal.FormulaManager

/**
 * Used within tests to inspect the values parent [Formula] passes to the child [Formula] and
 * to send child output events to the parent [Formula]
 */
class TestFormulaManager<Input, State, Output, RenderModel>(
    private val renderModel: RenderModel
) : FormulaManager<Input, State, Output, RenderModel> {

    private var transitionListener: ((Output?, Boolean) -> Unit) = { _, _ -> Unit }
    private val inputs = mutableListOf<Input>()

    override fun setTransitionListener(listener: (Output?, Boolean) -> Unit) {
        transitionListener = listener
    }

    override fun updateTransitionNumber(number: Long) {
        // no-op
    }

    override fun evaluate(
        formula: Formula<Input, State, Output, RenderModel>,
        input: Input,
        currentTransition: Long
    ): Evaluation<RenderModel> {
        inputs.add(input)
        return Evaluation(renderModel = renderModel)
    }

    override fun terminateOldUpdates(currentTransition: Long): Boolean {
        return false
    }

    override fun startNewUpdates(currentTransition: Long): Boolean {
        return false
    }

    override fun processSideEffects(currentTransition: Long): Boolean {
        return false
    }

    override fun markAsTerminated() = Unit

    override fun clearSideEffects() = Unit

    fun output(output: Output) {
        transitionListener.invoke(output, false)
    }

    fun lastInput(): Input {
        return inputs.last()
    }
}

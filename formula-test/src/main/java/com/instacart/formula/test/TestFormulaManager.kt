package com.instacart.formula.test

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Effects
import com.instacart.formula.internal.FormulaManager

/**
 * Used within tests to inspect the values parent [Formula] passes to the child [Formula] and
 * to emit child messages to the parent [Formula].
 */
class TestFormulaManager<Input, State, RenderModel>(
    private val renderModel: RenderModel
) : FormulaManager<Input, RenderModel> {

    private var transitionListener: ((Effects?, Boolean) -> Unit) = { _, _ -> Unit }
    private val inputs = mutableListOf<Input>()

    override fun setTransitionListener(listener: (Effects?, isValid: Boolean) -> Unit) {
        transitionListener = listener
    }

    override fun updateTransitionNumber(number: Long) {
        // no-op
    }

    override fun evaluate(
        input: Input,
        transitionId: Long
    ): Evaluation<RenderModel> {
        inputs.add(input)
        return Evaluation(renderModel = renderModel)
    }

    override fun terminateDetachedChildren(currentTransition: Long) = false

    override fun terminateOldUpdates(currentTransition: Long): Boolean {
        return false
    }

    override fun startNewUpdates(currentTransition: Long): Boolean {
        return false
    }

    override fun markAsTerminated() = Unit

    override fun performTerminationSideEffects() = Unit

    fun lastInput(): Input {
        return inputs.last()
    }
}

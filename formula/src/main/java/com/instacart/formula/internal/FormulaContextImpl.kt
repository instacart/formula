package com.instacart.formula.internal

import com.instacart.formula.FormulaContext
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Transition
import com.instacart.formula.Update
import java.lang.IllegalStateException

class FormulaContextImpl<State, Output> internal constructor(
    private val processingPass: Long,
    private val delegate: Delegate<State, Output>,
    private val transitionCallback: TransitionCallbackWrapper<State, Output>
) : FormulaContext<State, Output> {

    val children = mutableMapOf<FormulaKey, List<Update>>()

    interface Delegate<State, Effect> {
        fun <ChildInput, ChildState, ChildOutput, ChildRenderModel> child(
            formula: Formula<ChildInput, ChildState, ChildOutput, ChildRenderModel>,
            input: ChildInput,
            key: FormulaKey,
            onEvent: Transition.Factory.(ChildOutput) -> Transition<State, Effect>,
            processingPass: Long
        ): Evaluation<ChildRenderModel>
    }

    override fun callback(wrap: Transition.Factory.() -> Transition<State, Output>): () -> Unit {
        ensureNotRunning()
        return {
            transitionCallback(wrap(Transition.Factory))
        }
    }

    override fun <UIEvent> eventCallback(wrap: Transition.Factory.(UIEvent) -> Transition<State, Output>): (UIEvent) -> Unit {
        ensureNotRunning()
        return {
            transitionCallback(wrap(Transition.Factory, it))
        }
    }

    override fun updates(init: FormulaContext.UpdateBuilder<State, Output>.() -> Unit): List<Update> {
        ensureNotRunning()
        val builder = FormulaContext.UpdateBuilder(transitionCallback)
        builder.init()
        return builder.updates
    }

    override fun <ChildInput, ChildState, ChildOutput, ChildRenderModel> child(
        key: String,
        formula: Formula<ChildInput, ChildState, ChildOutput, ChildRenderModel>,
        input: ChildInput,
        onEvent: Transition.Factory.(ChildOutput) -> Transition<State, Output>
    ): ChildRenderModel {
        ensureNotRunning()
        val formulaKey = FormulaKey(formula::class, key)
        if (children.containsKey(formulaKey)) {
            throw IllegalStateException("There already is a child with same key: $formulaKey. Use [key: String] parameter.")
        }

        val result = delegate.child(formula, input, formulaKey, onEvent, processingPass)
        children[formulaKey] = result.updates
        return result.renderModel
    }

    private fun ensureNotRunning() {
        if (transitionCallback.running) {
            throw IllegalStateException("cannot call this after evaluation finished.")
        }
    }
}

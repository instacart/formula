package com.instacart.formula.internal

import com.instacart.formula.Child
import com.instacart.formula.FormulaContext
import com.instacart.formula.Formula
import com.instacart.formula.Transition
import com.instacart.formula.Update
import java.lang.IllegalStateException

class FormulaContextImpl<State> internal constructor(
    private val processingPass: Long,
    callbacks: ScopedCallbacks,
    private val delegate: Delegate<State>,
    private val transitionCallback: TransitionCallbackWrapper<State>
) : FormulaContext<State>(callbacks) {

    private val childBuilder: Child<State, *, *> = Child<State, Any, Any>(this)

    interface Delegate<State> {
        fun <ChildInput, ChildState, ChildRenderModel> child(
            formula: Formula<ChildInput, ChildState, ChildRenderModel>,
            input: ChildInput,
            key: Any,
            processingPass: Long
        ): ChildRenderModel
    }

    override fun performTransition(transition: Transition<State>) {
        transitionCallback.invoke(transition)
    }

    override fun updates(init: UpdateBuilder<State>.() -> Unit): List<Update<*, *>> {
        ensureNotRunning()
        val builder = UpdateBuilder(transitionCallback)
        builder.init()
        return builder.updates
    }

    fun <ChildInput, ChildState, ChildRenderModel> child(
        key: Any,
        formula: Formula<ChildInput, ChildState, ChildRenderModel>,
        input: ChildInput
    ): ChildRenderModel {
        ensureNotRunning()
        return delegate.child(formula, input, key, processingPass)
    }

    override fun <ChildInput, ChildState, ChildRenderModel> child(
        key: String,
        formula: Formula<ChildInput, ChildState, ChildRenderModel>
    ): Child<State, ChildInput, ChildRenderModel>  {
        ensureNotRunning()
        @Suppress("UNCHECKED_CAST")
        val casted = childBuilder as Child<State, ChildInput, ChildRenderModel>
        casted.initialize(key, formula)
        return casted
    }

    private fun ensureNotRunning() {
        if (transitionCallback.running) {
            throw IllegalStateException("cannot call this after evaluation finished.")
        }
    }
}

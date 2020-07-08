package com.instacart.formula.internal

import com.instacart.formula.FormulaContext
import com.instacart.formula.IFormula
import com.instacart.formula.Transition
import com.instacart.formula.Update
import java.lang.IllegalStateException

class FormulaContextImpl<State> internal constructor(
    private val processingPass: Long,
    callbacks: ScopedCallbacks,
    private val delegate: Delegate,
    private val transitionCallback: TransitionCallbackWrapper<State>
) : FormulaContext<State>(callbacks) {

    interface Delegate {
        fun <ChildInput, ChildRenderModel> child(
            formula: IFormula<ChildInput, ChildRenderModel>,
            input: ChildInput,
            key: Any,
            processingPass: Long
        ): ChildRenderModel
    }

    override fun performTransition(transition: Transition<State>) {
        transitionCallback.invoke(transition)
    }

    override fun updates(init: UpdateBuilder<State>.() -> Unit): List<Update<*>> {
        ensureNotRunning()
        val builder = UpdateBuilder(transitionCallback)
        builder.init()
        return builder.updates
    }

    override fun <ChildInput, ChildRenderModel> child(
        key: Any,
        formula: IFormula<ChildInput, ChildRenderModel>,
        input: ChildInput
    ): ChildRenderModel {
        ensureNotRunning()
        return delegate.child(formula, input, key, processingPass)
    }

    private fun ensureNotRunning() {
        if (transitionCallback.running) {
            throw IllegalStateException("cannot call this after evaluation finished.")
        }
    }
}

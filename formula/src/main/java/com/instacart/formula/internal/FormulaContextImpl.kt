package com.instacart.formula.internal

import com.instacart.formula.FormulaContext
import com.instacart.formula.IFormula
import com.instacart.formula.Transition
import com.instacart.formula.Update
import java.lang.IllegalStateException

class FormulaContextImpl<State> internal constructor(
    private val transitionId: TransitionId,
    callbacks: ScopedCallbacks,
    private val delegate: Delegate,
    private val transitionCallback: TransitionCallbackWrapper<State>
) : FormulaContext<State>(callbacks) {

    interface Delegate {
        fun <ChildInput, ChildOutput> child(
            formula: IFormula<ChildInput, ChildOutput>,
            input: ChildInput,
            transitionId: TransitionId
        ): ChildOutput
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

    override fun <ChildInput, ChildOutput> child(
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput
    ): ChildOutput {
        ensureNotRunning()
        return delegate.child(formula, input, transitionId)
    }

    private fun ensureNotRunning() {
        if (transitionCallback.running) {
            throw IllegalStateException("cannot call this transition after evaluation finished. See https://instacart.github.io/formula/faq/#after-evaluation-finished")
        }
    }
}

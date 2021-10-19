package com.instacart.formula.internal

import com.instacart.formula.FormulaContext
import com.instacart.formula.IFormula
import com.instacart.formula.BoundStream
import com.instacart.formula.Snapshot
import com.instacart.formula.StreamBuilder
import java.lang.IllegalStateException

class SnapshotImpl<Input, State> internal constructor(
    private val transitionId: TransitionId,
    listeners: ScopedListeners,
    private val delegate: Delegate,
    transitionDispatcher: TransitionDispatcher<Input, State>
) : FormulaContext<State>(listeners, transitionDispatcher), Snapshot<Input, State> {

    override val input: Input = transitionDispatcher.input
    override val state: State = transitionDispatcher.state
    override val context: FormulaContext<State> = this

    interface Delegate {
        fun <ChildInput, ChildOutput> child(
            formula: IFormula<ChildInput, ChildOutput>,
            input: ChildInput,
            transitionId: TransitionId
        ): ChildOutput
    }

    override fun updates(init: StreamBuilder<State>.() -> Unit): List<BoundStream<*>> {
        ensureNotRunning()
        val builder = StreamBuilder(this)
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
        if (transitionDispatcher.running) {
            throw IllegalStateException("Cannot call this transition after evaluation finished. See https://instacart.github.io/formula/faq/#after-evaluation-finished")
        }
    }
}

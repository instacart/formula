package com.instacart.formula.internal

import com.instacart.formula.ActionBuilder
import com.instacart.formula.FormulaContext
import com.instacart.formula.IFormula
import com.instacart.formula.DeferredAction
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot
import com.instacart.formula.Transition
import com.instacart.formula.TransitionContext
import java.lang.IllegalStateException
import kotlin.reflect.KClass

internal class SnapshotImpl<out Input, State> internal constructor(
    override val input: Input,
    override val state: State,
    private val transitionID: Long,
    listeners: Listeners,
    private val delegate: FormulaManagerImpl<Input, State, *>,
) : FormulaContext<Input, State>(listeners), Snapshot<Input, State>, TransitionContext<Input, State> {

    private var scopeKey: Any? = null
    var running = false
    var terminated = false

    override val context: FormulaContext<Input, State> = this

    override fun actions(init: ActionBuilder<Input, State>.() -> Unit): Set<DeferredAction<*>> {
        ensureNotRunning()
        val builder = ActionBuilderImpl(this)
        builder.init()
        return builder.actions
    }

    override fun <ChildInput, ChildOutput> child(
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput
    ): ChildOutput {
        ensureNotRunning()

        val key = createScopedKey(
            type = formula.type(),
            key = formula.key(input)
        )
        return delegate.child(key, formula, input)
    }

    override fun <Event> eventListener(
        key: Any,
        useIndex: Boolean,
        transition: Transition<Input, State, Event>
    ): Listener<Event> {
        ensureNotRunning()
        val listener = listeners.initOrFindListener<Input, State, Event>(key, useIndex)
        listener.manager = delegate
        listener.snapshotImpl = this
        listener.transition = transition
        return listener
    }

    override fun enterScope(key: Any) {
        ensureNotRunning()

        scopeKey = scopeKey?.let { JoinedKey(it, key) } ?: key
    }

    override fun endScope() {
        ensureNotRunning()

        if (scopeKey == null) {
            throw IllegalStateException("Cannot end root scope.")
        }

        val lastKey = (scopeKey as? JoinedKey)?.left
        scopeKey = lastKey
    }

    override fun createScopedKey(type: KClass<*>, key: Any?): Any {
        if (scopeKey == null && key == null) {
            // No need to allocate a new object, just use type as key.
            return type.java
        }

        return FormulaKey(
            scopeKey = scopeKey,
            type = type.java,
            key = key,
        )
    }

    fun dispatch(transition: Transition.Result<State>) {
        if (!running) {
            throw IllegalStateException("Transitions are not allowed during evaluation")
        }

        if (TransitionUtils.isEmpty(transition)) {
            return
        }

        if (!terminated && delegate.isEvaluationNeeded(transitionID)) {
            // We have already transitioned, this should not happen.
            throw IllegalStateException("Transition already happened. This is using old event listener: $transition. Transition: $transitionID != ${delegate.transitionID}")
        }

        delegate.handleTransitionResult(transition)
    }

    private fun ensureNotRunning() {
        if (running) {
            throw IllegalStateException("Cannot call this transition after evaluation finished. See https://instacart.github.io/formula/faq/#after-evaluation-finished")
        }
    }
}

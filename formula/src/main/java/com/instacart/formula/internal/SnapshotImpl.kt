package com.instacart.formula.internal

import com.instacart.formula.ActionBuilder
import com.instacart.formula.FormulaContext
import com.instacart.formula.IFormula
import com.instacart.formula.DeferredAction
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot
import com.instacart.formula.Transition
import com.instacart.formula.TransitionContext
import com.instacart.formula.lifecycle.LifecycleCache
import java.lang.IllegalStateException

internal class SnapshotImpl<out Input, State>(
    private val delegate: FormulaManagerImpl<Input, State, *>,
    override val input: Input,
    override val state: State,
    lifecycleCache: LifecycleCache,
) : FormulaContext<Input, State>(lifecycleCache), Snapshot<Input, State>, TransitionContext<Input, State> {

    override val effectDelegate: EffectDelegate = delegate
    override val context: FormulaContext<Input, State> = this

    private var scopeKey: Any? = null
    private var isEvaluationFinished = false

    override fun actions(init: ActionBuilder<Input, State>.() -> Unit): Set<DeferredAction<*>> {
        ensureEvaluationNotFinished()

        val builder = ActionBuilderImpl(delegate, this, lifecycleCache)
        builder.init()

        return emptySet()
    }

    override fun <ChildInput, ChildOutput> child(
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput
    ): ChildOutput {
        ensureEvaluationNotFinished()

        val key = createScopedKey(
            type = formula.type(),
            key = formula.key(input)
        )
        return delegate.child(key, formula, input)
    }

    override fun <ChildInput, ChildOutput> childOrNull(
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput,
    ): ChildOutput? {
        ensureEvaluationNotFinished()

        val key = createScopedKey(
            type = formula.type(),
            key = formula.key(input)
        )
        return delegate.childOrNull(key, formula, input)
    }

    override fun <Event> eventListener(
        key: Any,
        useIndex: Boolean,
        executionType: Transition.ExecutionType?,
        transition: Transition<Input, State, Event>
    ): Listener<Event> {
        ensureEvaluationNotFinished()
        val listener = lifecycleCache.findOrInit(key, useIndex) {
            ListenerImpl(transition)
        }
        applySnapshot(listener, executionType, transition)
        return listener
    }

    override fun enterScope(key: Any) {
        ensureEvaluationNotFinished()

        scopeKey = scopeKey?.let { JoinedKey(it, key) } ?: key
    }

    override fun endScope() {
        ensureEvaluationNotFinished()

        if (scopeKey == null) {
            throw IllegalStateException("Cannot end root scope.")
        }

        val lastKey = (scopeKey as? JoinedKey)?.left
        scopeKey = lastKey
    }

    override fun createScopedKey(type: Class<*>, key: Any?): Any {
        if (scopeKey == null && key == null) {
            // No need to allocate a new object, just use type as key.
            return type
        }

        return FormulaKey(
            scopeKey = scopeKey,
            type = type,
            key = key,
        )
    }

    fun <Event> dispatch(transition: Transition<Input, State, Event>, event: Event) {
        val result = transition.toResult(this, event)
        if (TransitionUtils.isEmpty(result)) {
            return
        }

        delegate.handleTransitionResult(event, result)
    }

    fun onEvaluationFinished() {
        isEvaluationFinished = true
    }

    override fun ensureEvaluationNotFinished() {
        if (isEvaluationFinished) {
            throw IllegalStateException("Cannot call this transition after evaluation finished. See https://instacart.github.io/formula/faq/#after-evaluation-finished")
        }
    }

    /**
     * Applies latest snapshot to the listener.
     */
    internal fun <Event> applySnapshot(
        listener: ListenerImpl<@UnsafeVariance Input, State, Event>,
        executionType: Transition.ExecutionType?,
        transition: Transition<Input, State, Event>
    ) {
        listener.setDependencies(delegate, this@SnapshotImpl, executionType, transition)
    }
}

package com.instacart.formula.internal

import com.instacart.formula.ActionBuilder
import com.instacart.formula.FormulaContext
import com.instacart.formula.IFormula
import com.instacart.formula.DeferredAction
import com.instacart.formula.Snapshot
import java.lang.IllegalStateException
import kotlin.reflect.KClass

internal class SnapshotImpl<out Input, State> internal constructor(
    private val transitionId: TransitionId,
    listeners: Listeners,
    private val delegate: FormulaManagerImpl<Input, State, *>,
    transitionDispatcher: TransitionDispatcher<Input, State>
) : FormulaContext<Input, State>(listeners, transitionDispatcher), Snapshot<Input, State> {

    override val input: Input = transitionDispatcher.input
    override val state: State = transitionDispatcher.state
    override val context: FormulaContext<Input, State> = this

    private var scopeKey: Any? = null

    @Deprecated("see parent", replaceWith = ReplaceWith("actions"))
    override fun updates(init: ActionBuilder<Input, State>.() -> Unit): List<DeferredAction<*>> {
        return actions(init)
    }

    override fun actions(init: ActionBuilder<Input, State>.() -> Unit): List<DeferredAction<*>> {
        ensureNotRunning()
        val builder = ActionBuilderImpl(this)
        builder.init()
        return builder.boundedActions
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
        return delegate.child(key, formula, input, transitionId)
    }

    override fun enterScope(key: Any) {
        scopeKey = scopeKey?.let { JoinedKey(it, key) } ?: key
    }

    override fun endScope() {
        if (scopeKey == null) {
            throw IllegalStateException("Cannot end root scope.")
        }

        val lastKey = (scopeKey as? JoinedKey)?.left
        scopeKey = lastKey
    }

    override fun createScopedKey(type: KClass<*>, key: Any?): Any {
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

    override fun ensureNotRunning() {
        if (transitionDispatcher.running) {
            throw IllegalStateException("Cannot call this transition after evaluation finished. See https://instacart.github.io/formula/faq/#after-evaluation-finished")
        }
    }
}

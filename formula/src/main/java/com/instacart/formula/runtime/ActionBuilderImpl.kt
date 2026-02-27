package com.instacart.formula.runtime

import com.instacart.formula.Action
import com.instacart.formula.ActionBuilder
import com.instacart.formula.Transition
import com.instacart.formula.action.ActionComponent
import com.instacart.formula.events.ListenerImpl
import com.instacart.formula.lifecycle.LifecycleCache

/**
 * Implements [ActionBuilder] interface.
 */
internal class ActionBuilderImpl<out Input, State> internal constructor(
    private val manager: FormulaManagerImpl<Input, State, *>,
    private val snapshot: SnapshotImpl<Input, State>,
    private val lifecycleCache: LifecycleCache,
) : ActionBuilder<Input, State>(
    input = snapshot.input,
    state = snapshot.state,
) {

    override fun <Event> events(
        action: Action<Event>,
        executionType: Transition.ExecutionType?,
        transition: Transition<Input, State, Event>,
    ) {
        updateOrInitActionComponent(action, executionType, transition)
    }

    override fun <Event> Action<Event>.onEvent(
        transition: Transition<Input, State, Event>,
    ) {
        events(this, null, transition)
    }

    override fun <Event> Action<Event>.onEventWithExecutionType(
        executionType: Transition.ExecutionType?,
        transition: Transition<Input, State, Event>
    ) {
        val stream = this
        events(stream, executionType, transition)
    }

    private fun <Event> updateOrInitActionComponent(
        stream: Action<Event>,
        executionType: Transition.ExecutionType?,
        transition: Transition<Input, State, Event>,
    ) {
        val key = snapshot.context.createScopedKey(transition.type(), stream.key())
        val action = lifecycleCache.findOrInit(key, useIndex = false) {
            val listener = ListenerImpl(transition)
            ActionComponent(manager, stream, listener)
        }
        snapshot.applySnapshot(action.listener, executionType, transition)
    }
}

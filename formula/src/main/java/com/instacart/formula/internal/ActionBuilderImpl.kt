package com.instacart.formula.internal

import com.instacart.formula.Action
import com.instacart.formula.ActionBuilder
import com.instacart.formula.DeferredAction
import com.instacart.formula.Snapshot
import com.instacart.formula.Transition

/**
 * Implements [ActionBuilder] interface.
 */
internal class ActionBuilderImpl<out Input, State> internal constructor(
    private val snapshot: Snapshot<Input, State>,
) : ActionBuilder<Input, State>(
    input = snapshot.input,
    state = snapshot.state,
) {
    internal val actions = LinkedHashSet<DeferredAction<*>>()

    override fun <Event> events(
        action: Action<Event>,
        executionType: Transition.ExecutionType?,
        transition: Transition<Input, State, Event>,
    ) {
        actions.add(toBoundStream(action, executionType, transition))
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

    private fun <Event> toBoundStream(
        stream: Action<Event>,
        executionType: Transition.ExecutionType? = null,
        transition: Transition<Input, State, Event>,
    ): DeferredAction<Event> {
        val key = snapshot.context.createScopedKey(transition.type(), stream.key())
        val listener = snapshot.context.eventListener(key, useIndex = false, executionType, transition)
        return DeferredAction(
            key = key,
            action = stream,
            initial = listener
        )
    }
}
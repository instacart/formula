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
        transition: Transition<Input, State, Event>,
    ) {
        add(toBoundStream(action, transition))
    }

    override fun <Event> onEvent(
        action: Action<Event>,
        avoidParameterClash: Any,
        transition: Transition<Input, State, Event>,
    ) {
        add(toBoundStream(action, transition))
    }

    override fun <Event> Action<Event>.onEvent(
        transition: Transition<Input, State, Event>,
    ) {
        val stream = this
        this@ActionBuilderImpl.events(stream, transition)
    }

    private fun add(action: DeferredAction<*>) {
        if (actions.contains(action)) {
            throw IllegalStateException("duplicate stream with key: ${action.keyAsString()}")
        }

        actions.add(action)
    }

    private fun <Event> toBoundStream(
        stream: Action<Event>,
        transition: Transition<Input, State, Event>,
    ): DeferredAction<Event> {
        val key = snapshot.context.createScopedKey(transition.type(), stream.key())
        val listener = snapshot.context.eventListener(key, useIndex = false, transition)
        return DeferredAction(
            key = key,
            action = stream,
            initial = listener
        )
    }
}
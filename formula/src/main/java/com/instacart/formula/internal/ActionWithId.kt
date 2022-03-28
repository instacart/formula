package com.instacart.formula.internal

import com.instacart.formula.Action
import com.instacart.formula.Cancelable

@PublishedApi
internal class ActionWithId<Event>(
    val id: Long,
    val delegateAction: Action<Event>,
) : Action<Event> {

    companion object {
        fun <Event> create(
            previous: ActionWithId<Event>?,
            delegateAction: Action<Event>,
        ): Action<Event> {
            val id = if (previous != null) {
                previous.id + 1
            } else {
                0
            }
            return ActionWithId(id, delegateAction)
        }
    }

    private val key = Pair(id, delegateAction.key())

    override fun start(send: (Event) -> Unit): Cancelable? = delegateAction.start(send)

    override fun key(): Any = key
}
package com.instacart.formula.action

import com.instacart.formula.Action
import com.instacart.formula.Cancelable

/**
 * Emits an event when [Formula] is terminated.
 */
internal object TerminateEventAction : Action<Unit> {
    override fun start(send: (Unit) -> Unit): Cancelable {
        return Cancelable {
            send(Unit)
        }
    }

    override fun key(): Any = Unit
}
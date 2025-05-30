package com.instacart.formula.action

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import kotlinx.coroutines.CoroutineScope

/**
 * Emits an event when [Formula] is terminated.
 */
internal object TerminateEventAction : Action<Unit> {
    override fun start(scope: CoroutineScope, send: (Unit) -> Unit): Cancelable {
        return Cancelable {
            send(Unit)
        }
    }

    override fun key(): Any = Unit
}

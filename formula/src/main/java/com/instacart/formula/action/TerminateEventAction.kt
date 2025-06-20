package com.instacart.formula.action

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import kotlinx.coroutines.CoroutineScope

/**
 * Emits an event when [Formula] is terminated.
 */
internal object TerminateEventAction : Action<Unit> {
    override fun start(scope: CoroutineScope, emitter: Action.Emitter<Unit>): Cancelable? {
        return Cancelable {
            emitter.onEvent(Unit)
        }
    }

    override fun key(): Any = Unit
}

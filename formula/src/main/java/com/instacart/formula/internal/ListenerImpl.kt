package com.instacart.formula.internal

import com.instacart.formula.Listener
import com.instacart.formula.Transition

/**
 * Note: this class is not a data class because equality is based on instance and not [key].
 */
@PublishedApi
internal class ListenerImpl<Input, State, EventT>(internal var key: Any) : Listener<EventT> {

    internal var manager: FormulaManagerImpl<Input, State, *>? = null
    internal var snapshotImpl: SnapshotImpl<Input, State>? = null
    internal var transition: Transition<Input, State, EventT>? = null

    override fun invoke(event: EventT) {
        val manager = manager ?: return
        val formulaEvent = Event {
            snapshotImpl?.let { snapshot ->
                transition?.let { transition ->
                    val result = transition.toResult(snapshot, event)
                    snapshot.dispatch(result)
                }
            }
        }
        manager.onEvent(formulaEvent)
        // TODO: log if null listener (it might be due to formula removal or due to callback removal)
    }

    fun disable() {
        manager = null
        snapshotImpl = null
        transition = null
    }
}

/**
 * A wrapper to convert Listener<Unit> from (Unit) -> Unit into () -> Unit
 */
internal data class UnitListener(val delegate: Listener<Unit>): () -> Unit {
    override fun invoke() = delegate(Unit)
}
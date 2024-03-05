package com.instacart.formula.internal

import com.instacart.formula.Listener
import com.instacart.formula.Transition
import com.instacart.formula.plugin.Dispatcher

/**
 * Note: this class is not a data class because equality is based on instance and not [key].
 */
@PublishedApi
internal class ListenerImpl<Input, State, EventT>(internal var key: Any) : Listener<EventT> {

    @Volatile internal var manager: FormulaManagerImpl<Input, State, *>? = null
    @Volatile internal var snapshotImpl: SnapshotImpl<Input, State>? = null

    internal lateinit var transition: Transition<Input, State, EventT>

    override fun invoke(event: EventT) {
        // TODO: log if null listener (it might be due to formula removal or due to callback removal)
        val manager = manager ?: return

        val dispatcher = manager.defaultDispatcher
        dispatcher.dispatch {
            manager.queue.postUpdate {
                val deferredTransition = DeferredTransition(this, transition, event)
                manager.onPendingTransition(deferredTransition)
            }
        }
    }

    fun disable() {
        manager = null
        snapshotImpl = null
    }
}

/**
 * A wrapper to convert Listener<Unit> from (Unit) -> Unit into () -> Unit
 */
internal data class UnitListener(private val delegate: Listener<Unit>): () -> Unit {
    override fun invoke() = delegate(Unit)
}
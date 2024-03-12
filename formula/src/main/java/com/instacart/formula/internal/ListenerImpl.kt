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
    @Volatile internal var executionType: Transition.ExecutionType? = null

    internal lateinit var transition: Transition<Input, State, EventT>

    override fun invoke(event: EventT) {
        // TODO: log if null listener (it might be due to formula removal or due to callback removal)
        val manager = manager ?: return

        when (val type = executionType) {
            is Transition.Batched -> handleBatched(type, event)
            Transition.Immediate -> executeWithDispatcher(Dispatcher.None, event)
            Transition.Background -> executeWithDispatcher(Dispatcher.Background, event)
            // If transition does not specify dispatcher, we use the default one.
            else -> executeWithDispatcher(manager.defaultDispatcher, event)
        }
    }

    fun disable() {
        manager = null
        snapshotImpl = null
    }

    private fun handleBatched(type: Transition.Batched, event: EventT) {
        val manager = manager ?: return

        manager.batchManager.add(type, event) {
            val deferredTransition = DeferredTransition(this, transition, event)
            manager.onPendingTransition(deferredTransition)
        }
    }

    private fun executeWithDispatcher(dispatcher: Dispatcher, event: EventT) {
        val manager = manager ?: return

        dispatcher.dispatch {
            manager.queue.postUpdate {
                val deferredTransition = DeferredTransition(this, transition, event)
                manager.onPendingTransition(deferredTransition)
            }
        }
    }
}

/**
 * A wrapper to convert Listener<Unit> from (Unit) -> Unit into () -> Unit
 */
internal data class UnitListener(private val delegate: Listener<Unit>): () -> Unit {
    override fun invoke() = delegate(Unit)
}
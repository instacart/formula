package com.instacart.formula.internal

import com.instacart.formula.Listener
import com.instacart.formula.Transition
import com.instacart.formula.lifecycle.LifecycleComponent
import com.instacart.formula.lifecycle.LifecycleScheduler
import com.instacart.formula.plugin.Dispatcher

/**
 * Note: this class is not a data class because equality is based on instance.
 */
@PublishedApi
internal class ListenerImpl<Input, State, EventT>(
    private var transition: Transition<Input, State, EventT>
) : Listener<EventT>, LifecycleComponent {

    @Volatile private var manager: FormulaManagerImpl<Input, State, *>? = null
    @Volatile private var snapshotImpl: SnapshotImpl<Input, State>? = null
    @Volatile private var executionType: Transition.ExecutionType? = null

    override fun invoke(event: EventT) {
        // TODO: log if null listener (it might be due to formula removal or due to callback removal)
        val manager = manager ?: return
        if (!manager.isEventHandlingEnabled) return

        when (val type = executionType) {
            is Transition.Batched -> handleBatched(manager, type, event)
            Transition.Immediate -> executeWithDispatcher(manager, Dispatcher.None, event)
            Transition.Background -> executeWithDispatcher(manager, Dispatcher.Background, event)
            // If transition does not specify dispatcher, we use the default one.
            else -> executeWithDispatcher(manager, manager.defaultDispatcher, event)
        }
    }

    override fun onDetached(scheduler: LifecycleScheduler) {
        manager = null
        snapshotImpl = null
    }

    fun setDependencies(
        manager: FormulaManagerImpl<Input, State, *>?,
        snapshot: SnapshotImpl<Input, State>?,
        executionType: Transition.ExecutionType?,
        transition: Transition<Input, State, EventT>,
    ) {
        this.manager = manager
        this.snapshotImpl = snapshot
        this.executionType = executionType
        this.transition = transition
    }

    fun applyInternal(event: EventT) {
        snapshotImpl?.dispatch(transition, event)
    }

    private fun handleBatched(
        manager: FormulaManagerImpl<Input, State, *>,
        type: Transition.Batched,
        event: EventT,
    ) {
        manager.batchManager.add(type, event) {
            val deferredTransition = DeferredTransition(this, event)
            manager.onPendingTransition(deferredTransition)
        }
    }

    private fun executeWithDispatcher(
        manager: FormulaManagerImpl<Input, State, *>,
        dispatcher: Dispatcher,
        event: EventT,
    ) {
        manager.queue.postUpdate(dispatcher) {
            val deferredTransition = DeferredTransition(this, event)
            manager.onPendingTransition(deferredTransition)
        }
    }
}

/**
 * A wrapper to convert Listener<Unit> from (Unit) -> Unit into () -> Unit
 */
internal data class UnitListener(private val delegate: Listener<Unit>): () -> Unit {
    override fun invoke() = delegate(Unit)
}
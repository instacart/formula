package com.instacart.formula.action

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import com.instacart.formula.DeferredAction
import com.instacart.formula.events.ListenerImpl
import com.instacart.formula.lifecycle.LifecycleComponent
import com.instacart.formula.lifecycle.LifecycleScheduler

/**
 * Manages the lifecycle of [Action].
 */
internal class ActionComponent<Input, State, Event> internal constructor(
    private val delegate: ActionDelegate,
    private val action: Action<Event>,
    internal val listener: ListenerImpl<Input, State, Event>,
) : DeferredAction<Event>, Action.Emitter<Event>, LifecycleComponent {
    @Volatile private var isTerminated = false
    private var cancelable: Cancelable? = null

    // ==========================================================================
    // LifecycleComponent
    // ==========================================================================

    override fun onAttached(scheduler: LifecycleScheduler) {
        scheduler.scheduleStartEffect(this::start)
    }

    override fun onDetached(scheduler: LifecycleScheduler) {
        scheduler.scheduleTerminateEffect(this::performTermination)
    }

    override fun performTermination() {
        delegate.inspector?.onActionFinished(delegate.formulaType, this)
        delegate.runSafe { cancelable?.cancel() }

        cancelable = null
        isTerminated = true
    }

    // ==========================================================================
    // Action.Emitter
    // ==========================================================================

    override fun onEvent(event: Event) {
        if (!isTerminated) {
            listener.invoke(event)
        }
    }

    override fun onError(throwable: Throwable) {
        delegate.onActionError(throwable)
    }

    // ==========================================================================
    // Internal
    // ==========================================================================

    internal fun start() {
        if (isTerminated) return

        delegate.inspector?.onActionStarted(delegate.formulaType, this)

        delegate.runSafe {
            cancelable = action.start(delegate.scope, this)
        }
    }
}

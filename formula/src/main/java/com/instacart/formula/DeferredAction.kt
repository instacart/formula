package com.instacart.formula

import com.instacart.formula.internal.ActionDelegate
import com.instacart.formula.internal.onActionError
import com.instacart.formula.internal.runSafe
import com.instacart.formula.plugin.FormulaError
import kotlinx.coroutines.CoroutineScope

/**
 * An action combined with event listener.
 */
class DeferredAction<Event> internal constructor(
    val key: Any,
    private val action: Action<Event>,
    // We use event listener for equality because it provides better equality performance
    private val listener: (Event) -> Unit,
    private val delegate: ActionDelegate,
) {
    @Volatile private var isTerminated = false
    private var cancelable: Cancelable? = null

    internal fun start() {
        delegate.inspector?.onActionStarted(delegate.formulaType, this)

        val emitter = object : Action.Emitter<Event> {
            override fun onEvent(event: Event) {
                if (!isTerminated) {
                    listener.invoke(event)
                }
            }

            override fun onError(throwable: Throwable) {
                delegate.onActionError(throwable)
            }
        }

        delegate.runSafe {
            cancelable = action.start(delegate.scope, emitter)
        }
    }

    internal fun tearDown() {
        delegate.inspector?.onActionFinished(delegate.formulaType, this)
        delegate.runSafe { cancelable?.cancel() }

        cancelable = null
        isTerminated = true
    }

    /**
     * Action equality is based on the [initial] listener.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is DeferredAction<*> && listener == other.listener
    }

    override fun hashCode(): Int {
        return listener.hashCode()
    }

    fun isTerminated(): Boolean = isTerminated
}

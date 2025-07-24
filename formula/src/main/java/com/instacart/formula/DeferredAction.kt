package com.instacart.formula

import com.instacart.formula.internal.ActionDelegate
import com.instacart.formula.internal.onActionError
import com.instacart.formula.internal.runSafe
import com.instacart.formula.plugin.FormulaError
import kotlinx.coroutines.CoroutineScope

/**
 * An action combined with event listener.
 */
class DeferredAction<Event>(
    val key: Any,
    private val action: Action<Event>,
    // We use event listener for equality because it provides better equality performance
    private val listener: (Event) -> Unit
) {
    @Volatile private var isEnabled = true
    private var cancelable: Cancelable? = null

    internal fun start(delegate: ActionDelegate) {
        val emitter = object : Action.Emitter<Event> {
            override fun onEvent(event: Event) {
                if (isEnabled) {
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

    internal fun tearDown(delegate: ActionDelegate) {
        delegate.runSafe {
            cancelable?.cancel()
        }

        cancelable = null
        isEnabled = false
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
}

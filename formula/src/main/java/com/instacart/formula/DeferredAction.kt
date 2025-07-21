package com.instacart.formula

import com.instacart.formula.plugin.FormulaError
import kotlinx.coroutines.CoroutineScope

/**
 * An action combined with event listener.
 */
class DeferredAction<Event>(
    val key: Any,
    private val action: Action<Event>,
    // We use event listener for equality because it provides better equality performance
    private val initial: (Event) -> Unit
) {
    private var cancelable: Cancelable? = null

    internal var listener: ((Event) -> Unit)? = initial

    internal fun start(scope: CoroutineScope, formulaType: Class<*>) {
        val emitter = object : Action.Emitter<Event> {
            override fun onEvent(event: Event) {
                listener?.invoke(event)
            }

            override fun onError(throwable: Throwable) {
                val error = FormulaError.ActionError(formulaType, throwable)
                FormulaPlugins.onError(error)
            }
        }

        try {
            cancelable = action.start(scope, emitter)
        } catch (throwable: Throwable) {
            val error = FormulaError.ActionError(formulaType, throwable)
            FormulaPlugins.onError(error)
        }

    }

    internal fun tearDown(formulaType: Class<*>) {
        try {
            cancelable?.cancel()
        } catch (throwable: Throwable) {
            val error = FormulaError.ActionError(formulaType, throwable)
            FormulaPlugins.onError(error)
        }

        cancelable = null
    }

    /**
     * Action equality is based on the [initial] listener.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is DeferredAction<*> && initial == other.initial
    }

    override fun hashCode(): Int {
        return initial.hashCode()
    }
}

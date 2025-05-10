package com.instacart.formula

/**
 * An action combined with event listener.
 */
class DeferredAction<Event>(
    val key: Any,
    private val action: Action<Event>,
    // We use event listener for equality because it provides better equality performance
    private val initial: (Event) -> Unit
) {
    // Set before [start] is called
    internal lateinit var formulaType: Class<*>

    private var cancelable: Cancelable? = null
    internal var listener: ((Event) -> Unit)? = initial

    internal fun start() {
        val emitter = object : Action.Emitter<Event> {
            override fun onEvent(event: Event) {
                listener?.invoke(event)
            }

            override fun onError(throwable: Throwable) {
                FormulaPlugins.onUnhandledActionError(formulaType, throwable)
            }
        }

        cancelable = action.start(emitter)
    }

    internal fun tearDown() {
        cancelable?.cancel()
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

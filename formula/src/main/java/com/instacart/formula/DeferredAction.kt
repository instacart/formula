package com.instacart.formula

/**
 * An action combined with event listener.
 */
class DeferredAction<Event>(
    val key: Any,
    val action: Action<Event>,
    // We use event listener for equality because it provides better equality performance
    val initial: (Event) -> Unit
) {

    internal var listener: (Event) -> Unit = initial
    internal var cancelable: Cancelable? = null

    internal fun start() {
        cancelable = action.start() { message ->
            listener.invoke(message)
        }
    }

    internal fun tearDown() {
        cancelable?.cancel()
        cancelable = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeferredAction<*>

        if (initial != other.initial) return false

        return true
    }

    override fun hashCode(): Int {
        return initial.hashCode()
    }

    fun keyAsString(): String {
        return key.toString()
    }
}

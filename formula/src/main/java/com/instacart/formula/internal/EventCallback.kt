package com.instacart.formula.internal

/**
 * Note: this class is not a data class because equality is based on instance and not [key].
 */
class EventCallback<T>(internal val key: Any): (T) -> Unit {
    @PublishedApi internal var callback: ((T) -> Unit)? = null

    override fun invoke(p1: T) {
        callback?.invoke(p1)
        // TODO: log if null callback
    }
}

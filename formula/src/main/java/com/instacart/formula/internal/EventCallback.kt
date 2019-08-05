package com.instacart.formula.internal

/**
 * Note: this class is not a data class because equality is based on instance and not [key].
 */
class EventCallback<T>(val key: Any): (T) -> Unit {
    @PublishedApi internal lateinit var callback: (T) -> Unit

    override fun invoke(p1: T) {
        callback(p1)
    }
}

package com.instacart.formula.internal

/**
 * Holder tracks when object has already been request and throws an error if requested again. This
 * is used to track duplicate requests for a particular key.
 */
internal class SingleRequestHolder<T : Any>(val key: Any) {
    private var _value: T? = null

    val value: T
        get() = requireNotNull(_value)

    var requested: Boolean = false

    fun reset() {
        requested = false
    }

    fun isNew(): Boolean {
        return _value == null
    }

    inline fun requestOrInitValue(factory: () -> T): T {
        requestOrThrow()

        return _value ?: run {
            val initialized = factory()
            save(initialized)
        }
    }

    fun save(value: T): T {
        _value = value
        return value
    }

    fun requestOrThrow() {
        if (requested) {
            throw IllegalStateException("Entry with $key is already defined.")
        }

        requested = true
    }
}

internal typealias SingleRequestMap<Key, Value> = MutableMap<Key, SingleRequestHolder<Value>>

internal fun <Key : Any, Value : Any> SingleRequestMap<Key, Value>.clearUnrequested(onUnrequested: (Key, Value) -> Unit) {
    val callbackIterator = this.iterator()
    while (callbackIterator.hasNext()) {
        val entry = callbackIterator.next()
        if (!entry.value.requested) {
            onUnrequested(entry.key, entry.value.value)
            callbackIterator.remove()
        } else {
            entry.value.reset()
        }
    }
}

internal inline fun <Value : Any> SingleRequestMap<*, Value>.forEachValue(callback: (Value) -> Unit) {
    forEach {
        callback(it.value.value)
    }
}

package com.instacart.formula.internal

/**
 * Holder tracks when object has already been request and throws an error if requested again. This
 * is used to track duplicate requests for a particular key.
 */
internal class SingleRequestHolder<T>(val value: T) {
    var requested: Boolean = false

    inline fun requestAccess(errorMessage: () -> String): T {
        if (requested) {
            throw IllegalStateException(errorMessage())
        }

        requested = true
        return value
    }

    fun reset() {
        requested = false
    }
}

internal typealias SingleRequestMap<Key, Value> = MutableMap<Key, SingleRequestHolder<Value>>

internal fun <Value> SingleRequestMap<*, Value>.clearUnrequested(onUnrequested: (Value) -> Unit) {
    val callbackIterator = this.iterator()
    while (callbackIterator.hasNext()) {
        val callback = callbackIterator.next()
        if (!callback.value.requested) {
            onUnrequested(callback.value.value)
            callbackIterator.remove()
        } else {
            callback.value.reset()
        }
    }
}

internal inline fun <Value> SingleRequestMap<*, Value>.forEachValue(callback: (Value) -> Unit) {
    forEach {
        callback(it.value.value)
    }
}

internal inline fun <Key, Value> SingleRequestMap<Key, Value>.findOrInit(
    key: Key,
    create: () -> Value
): SingleRequestHolder<Value> {
    return getOrPut(key) {
        SingleRequestHolder(create())
    }
}

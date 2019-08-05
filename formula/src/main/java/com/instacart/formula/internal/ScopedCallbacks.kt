package com.instacart.formula.internal

import com.instacart.formula.Formula

internal class ScopedCallbacks private constructor(
    private val rootKey: Any
) {
    constructor(formula: Formula<*, *, *, *>) : this(formula::class)

    private val callbacks: SingleRequestMap<Any, Callbacks> = mutableMapOf()

    private var lastKey: Any? = null
    private var currentKey: Any = rootKey
    private var current: Callbacks? = null

    fun initOrFindCallback(key: Any): Callback {
        return currentCallbacks().initOrFindCallback(key)
    }

    fun initOrFindPositionalCallback(): Callback {
        return currentCallbacks().initOrFindPositionalCallback()
    }

    fun initOrFindOptionalCallback(condition: Boolean): Callback? {
        return currentCallbacks().initOrFindOptionalCallback(condition)
    }

    fun <UIEvent> initOrFindEventCallback(key: Any): EventCallback<UIEvent> {
        return currentCallbacks().initOrFindEventCallback(key)
    }

    fun <UIEvent> initOrFindPositionalEventCallback(): EventCallback<UIEvent> {
        return currentCallbacks().initOrFindPositionalEventCallback()
    }

    fun <UIEvent> initOrFindOptionalEventCallback(condition: Boolean): EventCallback<UIEvent>? {
        return currentCallbacks().initOrFindOptionalEventCallback(condition)
    }

    fun enterScope(key: Any) {
        lastKey = currentKey
        currentKey = JoinedKey(lastKey, key)
        current = null
    }

    fun endScope() {
        if (currentKey == rootKey) {
            throw IllegalStateException("Cannot end root scope.")
        }

        currentKey = lastKey ?: rootKey
    }

    fun evaluationFinished() {
        val iterator = callbacks.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (!entry.value.requested) {
                entry.value.value.disableAll()
                iterator.remove()
            } else {
                val callbacks = entry.value.value
                if (!callbacks.isValidRound()) {
                    val message = buildString {
                        append("Dynamic callback registrations detected in ${entry.key}. ")
                        append("Expected: ${callbacks.lastCallbackCount}, was: ${callbacks.callbackCount}.")
                        append("Take a look at https://github.com/instacart/formula/blob/master/docs/Getting-Started.md#callbacks")
                    }
                    throw IllegalStateException(message)
                }

                callbacks.evaluationFinished()
                entry.value.requested = false
            }
        }

        current = null
    }

    fun disableAll() {
        callbacks.forEachValue {
            it.disableAll()
        }
        callbacks.clear()
    }

    private fun currentCallbacks(): Callbacks {
        return current ?: callbacks
            .findOrInit(currentKey) { Callbacks() }
            .requestAccess { "Duplicate key: $currentKey" }
            .apply {
                current = this
            }
    }
}

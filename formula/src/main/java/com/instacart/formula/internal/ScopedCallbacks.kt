package com.instacart.formula.internal

import com.instacart.formula.Formula

@PublishedApi
internal class ScopedCallbacks private constructor(
    private val rootKey: Any
) {
    constructor(formula: Formula<*, *, *, *>) : this(formula::class)

    private val callbacks: SingleRequestMap<Any, Callbacks> = mutableMapOf()

    private var lastKey: Any? = null
    private var currentKey: Any = rootKey
    private var current: Callbacks? = null

    internal var enabled: Boolean = false

    fun initOrFindCallback(key: Any): Callback {
        ensureNotRunning()
        return currentCallbacks().initOrFindCallback(key)
    }

    fun initOrFindPositionalCallback(): Callback {
        ensureNotRunning()
        return currentCallbacks().initOrFindPositionalCallback()
    }

    fun initOrFindOptionalCallback(condition: Boolean): Callback? {
        ensureNotRunning()
        return currentCallbacks().initOrFindOptionalCallback(condition)
    }

    fun <UIEvent> initOrFindEventCallback(key: Any): EventCallback<UIEvent> {
        ensureNotRunning()
        return currentCallbacks().initOrFindEventCallback(key)
    }

    fun <UIEvent> initOrFindPositionalEventCallback(): EventCallback<UIEvent> {
        ensureNotRunning()
        return currentCallbacks().initOrFindPositionalEventCallback()
    }

    fun <UIEvent> initOrFindOptionalEventCallback(condition: Boolean): EventCallback<UIEvent>? {
        ensureNotRunning()
        return currentCallbacks().initOrFindOptionalEventCallback(condition)
    }

    fun enterScope(key: Any) {
        ensureNotRunning()

        lastKey = currentKey
        currentKey = JoinedKey(lastKey, key)
        current = null
    }

    fun endScope() {
        ensureNotRunning()

        if (currentKey == rootKey) {
            throw IllegalStateException("Cannot end root scope.")
        }

        currentKey = lastKey ?: rootKey
    }

    fun evaluationStarted() {
        enabled = true
    }

    fun evaluationFinished() {
        enabled = false

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

    private fun ensureNotRunning() {
        if (!enabled) {
            throw java.lang.IllegalStateException("cannot call this after evaluation finished.")
        }
    }
}

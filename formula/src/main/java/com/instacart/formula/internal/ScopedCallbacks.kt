package com.instacart.formula.internal

import com.instacart.formula.IFormula

@PublishedApi
internal class ScopedCallbacks private constructor(
    private val rootKey: Any
) {
    constructor(formula: IFormula<*, *>) : this(formula::class)

    private var callbacks: SingleRequestMap<Any, Callbacks>? = null

    private var lastCallbacks: Callbacks? = null
    private var lastKey: Any? = null
    private var currentKey: Any = rootKey
    private var current: Callbacks? = null

    internal var enabled: Boolean = false

    fun initOrFindCallback(key: Any): Callback {
        ensureNotRunning()
        return currentCallbacks().initOrFindCallback(key)
    }

    fun <UIEvent> initOrFindEventCallback(key: Any): EventCallback<UIEvent> {
        ensureNotRunning()
        return currentCallbacks().initOrFindEventCallback(key)
    }

    fun enterScope(key: Any) {
        ensureNotRunning()
        if (currentKey != rootKey) {
            throw IllegalStateException("Nested scopes are not supported currently. Current scope: $currentKey")
        }

        lastKey = currentKey
        lastCallbacks = current
        currentKey = JoinedKey(lastKey, key)
        current = null
    }

    fun endScope() {
        ensureNotRunning()

        if (currentKey == rootKey) {
            throw IllegalStateException("Cannot end root scope.")
        }

        currentKey = lastKey ?: rootKey
        current = lastCallbacks

        lastKey = null
        lastCallbacks = null
    }

    fun evaluationStarted() {
        enabled = true
    }

    fun evaluationFinished() {
        enabled = false

        val iterator = callbacks?.iterator()
        if (iterator != null) {
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (!entry.value.requested) {
                    entry.value.value.disableAll()
                    iterator.remove()
                } else {
                    val callbacks = entry.value.value
                    callbacks.evaluationFinished()
                    entry.value.requested = false
                }
            }
        }

        current = null
    }

    fun disableAll() {
        callbacks?.forEachValue {
            it.disableAll()
        }
        callbacks?.clear()
    }

    private fun currentCallbacks(): Callbacks {
        val callbacks = callbacks ?: run {
            val initialized: SingleRequestMap<Any, Callbacks> = mutableMapOf()
            this.callbacks = initialized
            initialized
        }
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

package com.instacart.formula.internal

import com.instacart.formula.IFormula

@PublishedApi
internal class ScopedListeners private constructor(
    private val rootKey: Any
) {
    constructor(formula: IFormula<*, *>) : this(formula::class)

    private var listeners: SingleRequestMap<Any, Listeners>? = null

    private var lastListeners: Listeners? = null
    private var lastKey: Any? = null
    private var currentKey: Any = rootKey
    private var current: Listeners? = null

    internal var enabled: Boolean = false

    fun <Input, State, Event> initOrFindListener(key: Any): ListenerImpl<Input, State, Event> {
        ensureNotRunning()
        return currentCallbacks().initOrFindCallback(key)
    }

    fun enterScope(key: Any) {
        ensureNotRunning()
        if (currentKey != rootKey) {
            throw IllegalStateException("Nested scopes are not supported currently. Current scope: $currentKey")
        }

        lastKey = currentKey
        lastListeners = current
        currentKey = JoinedKey(lastKey, key)
        current = null
    }

    fun endScope() {
        ensureNotRunning()

        if (currentKey == rootKey) {
            throw IllegalStateException("Cannot end root scope.")
        }

        currentKey = lastKey ?: rootKey
        current = lastListeners

        lastKey = null
        lastListeners = null
    }

    fun evaluationStarted() {
        enabled = true
    }

    fun evaluationFinished() {
        enabled = false

        val iterator = listeners?.iterator()
        if (iterator != null) {
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (!entry.value.requested) {
                    entry.value.value.disableAll()
                    iterator.remove()
                } else {
                    val listeners = entry.value.value
                    listeners.evaluationFinished()
                    entry.value.requested = false
                }
            }
        }

        current = null
    }

    fun disableAll() {
        listeners?.forEachValue {
            it.disableAll()
        }
        listeners?.clear()
    }

    private fun currentCallbacks(): Listeners {
        val listeners = listeners ?: run {
            val initialized: SingleRequestMap<Any, Listeners> = mutableMapOf()
            this.listeners = initialized
            initialized
        }
        return current ?: listeners
            .findOrInit(currentKey) { Listeners() }
            .requestAccess { "Duplicate key: $currentKey" }
            .apply {
                current = this
            }
    }

    private fun ensureNotRunning() {
        if (!enabled) {
            throw java.lang.IllegalStateException("Cannot call this after evaluation finished. See https://instacart.github.io/formula/faq/#after-evaluation-finished")
        }
    }
}

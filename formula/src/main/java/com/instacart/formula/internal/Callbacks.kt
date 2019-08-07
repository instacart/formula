package com.instacart.formula.internal

internal class Callbacks {
    private val callbacks: SingleRequestMap<Any, Callback> = mutableMapOf()
    private val eventCallbacks: SingleRequestMap<Any, EventCallback<*>> = mutableMapOf()
    internal var lastCallbackCount = -1
    internal var callbackCount = 0

    fun initOrFindCallback(key: Any): Callback {
        return callbacks
            .findOrInit(key) { Callback(key) }
            .requestAccess {
                "Callback $key is already defined. Make sure your key is unique."
            }
    }

    fun initOrFindPositionalCallback(): Callback {
        val key = callbackCount
        val callback = initOrFindCallback(key)
        incrementCallbackCount()
        return callback
    }

    fun initOrFindOptionalCallback(condition: Boolean): Callback? {
        return if (condition) {
            initOrFindPositionalCallback()
        } else {
            incrementCallbackCount()
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <UIEvent> initOrFindEventCallback(key: Any): EventCallback<UIEvent> {
        return eventCallbacks
            .findOrInit(key) { EventCallback<UIEvent>(key) }
            .requestAccess {
                "Event callback $key is already defined. Make sure your key is unique."
            } as EventCallback<UIEvent>
    }

    fun <UIEvent> initOrFindPositionalEventCallback(): EventCallback<UIEvent> {
        val key = callbackCount
        incrementCallbackCount()
        return initOrFindEventCallback(key)
    }

    fun <UIEvent> initOrFindOptionalEventCallback(condition: Boolean): EventCallback<UIEvent>? {
        return if (condition) {
            initOrFindPositionalEventCallback()
        } else {
            incrementCallbackCount()
            null
        }
    }

    fun evaluationFinished() {
        callbacks.clearUnrequested {
            it.callback = {
                // TODO log that disabled callback was invoked.
            }
        }

        eventCallbacks.clearUnrequested {
            it.callback = {
                // TODO log that disabled callback was invoked.
            }
        }

        lastCallbackCount = callbackCount
        callbackCount = 0
    }

    fun disableAll() {
        callbacks.forEachValue {
            it.callback = {
                // TODO log that event is invalid because child was removed
            }
        }
        callbacks.clear()

        eventCallbacks.forEachValue { entry ->
            entry.callback = {
                // TODO log that event is invalid because child was removed
            }
        }
        eventCallbacks.clear()
    }

    fun isValidRound(): Boolean {
        return lastCallbackCount == -1 || lastCallbackCount == callbackCount
    }

    private fun incrementCallbackCount() {
        callbackCount += 1
    }
}

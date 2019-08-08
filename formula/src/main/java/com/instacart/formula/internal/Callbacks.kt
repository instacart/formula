package com.instacart.formula.internal

internal class Callbacks {
    private val callbacks: SingleRequestMap<Any, Callback> = mutableMapOf()
    private val eventCallbacks: SingleRequestMap<Any, EventCallback<*>> = mutableMapOf()

    fun initOrFindCallback(key: Any): Callback {
        return callbacks
            .findOrInit(key) { Callback(key) }
            .requestAccess {
                "Callback $key is already defined. Make sure your key is unique."
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
}

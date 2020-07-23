package com.instacart.formula.internal

internal class Callbacks {
    private var callbacks: SingleRequestMap<Any, Callback>? = null
    private var eventCallbacks: SingleRequestMap<Any, EventCallback<*>>? = null

    private fun duplicateKeyErrorMessage(key: Any): String {
        if (key is String) {
            // This indicates manual key creation.
            return "Callback $key is already defined. Make sure your key is unique."
        }
        // This indicates automatic key generation
        return "Callback $key is already defined. Are you calling it in a loop or reusing a method? You can wrap the call with FormulaContext.key"
    }

    fun initOrFindCallback(key: Any): Callback {
        val callbacks = callbacks ?: run {
            val initialized: SingleRequestMap<Any, Callback> = mutableMapOf()
            this.callbacks = initialized
            initialized
        }

        return callbacks
            .findOrInit(key) { Callback(key) }
            .requestAccess {
                duplicateKeyErrorMessage(key)
            }
    }

    @Suppress("UNCHECKED_CAST")
    fun <UIEvent> initOrFindEventCallback(key: Any): EventCallback<UIEvent> {
        val eventCallbacks = eventCallbacks ?: run {
            val initialized: SingleRequestMap<Any, EventCallback<*>> = mutableMapOf()
            this.eventCallbacks = initialized
            initialized
        }

        return eventCallbacks
            .findOrInit(key) { EventCallback<UIEvent>(key) }
            .requestAccess {
                duplicateKeyErrorMessage(key)
            } as EventCallback<UIEvent>
    }

    fun evaluationFinished() {
        callbacks?.clearUnrequested {
            it.callback = {
                // TODO log that disabled callback was invoked.
            }
        }

        eventCallbacks?.clearUnrequested {
            it.callback = {
                // TODO log that disabled callback was invoked.
            }
        }
    }

    fun disableAll() {
        callbacks?.forEachValue {
            it.callback = {
                // TODO log that event is invalid because child was removed
            }
        }
        callbacks?.clear()

        eventCallbacks?.forEachValue { entry ->
            entry.callback = {
                // TODO log that event is invalid because child was removed
            }
        }
        eventCallbacks?.clear()
    }
}

package com.instacart.formula.internal

internal class Callbacks {
    private var callbacks: SingleRequestMap<Any, Callback<*>>? = null

    private fun duplicateKeyErrorMessage(key: Any): String {
        if (key is String) {
            // This indicates manual key creation.
            return "Callback $key is already defined. Make sure your key is unique."
        }
        // This indicates automatic key generation
        return "Callback $key is already defined. Are you calling it in a loop or reusing a method? You can wrap the call with FormulaContext.key"
    }

    fun initOrFindCallbackT(key: Any): UnitCallback {
        val callbacks = callbacks ?: run {
            val initialized: SingleRequestMap<Any, Callback<*>> = mutableMapOf()
            this.callbacks = initialized
            initialized
        }

        return callbacks
            .findOrInit(key) { UnitCallback(key) }
            .requestAccess {
                duplicateKeyErrorMessage(key)
            } as UnitCallback
    }

    fun <UIEvent> initOrFindCallbackT(key: Any): Callback<UIEvent> {
        val callbacks = callbacks ?: run {
            val initialized: SingleRequestMap<Any, Callback<*>> = mutableMapOf()
            this.callbacks = initialized
            initialized
        }

        return callbacks
            .findOrInit(key) { Callback<UIEvent>(key) }
            .requestAccess {
                duplicateKeyErrorMessage(key)
            } as Callback<UIEvent>
    }

    fun evaluationFinished() {
        callbacks?.clearUnrequested {
            it.delegate = {
                // TODO log that disabled callback was invoked.
            }
        }
    }

    fun disableAll() {
        callbacks?.forEachValue {
            it.delegate = {
                // TODO log that event is invalid because child was removed
            }
        }
        callbacks?.clear()
    }
}

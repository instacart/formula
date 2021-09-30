package com.instacart.formula.internal

internal class Callbacks<State> {
    private var callbacks: SingleRequestMap<Any, CallbackImpl<State, *>>? = null

    private fun duplicateKeyErrorMessage(key: Any): String {
        if (key is String) {
            // This indicates manual key creation.
            return "Callback $key is already defined. Make sure your key is unique."
        }
        // This indicates automatic key generation
        return "Callback $key is already defined. Are you calling it in a loop or reusing a method? You can wrap the call with FormulaContext.key"
    }

    fun <Event> initOrFindCallback(key: Any): CallbackImpl<State, Event> {
        val callbacks = callbacks ?: run {
            val initialized: SingleRequestMap<Any, CallbackImpl<State, *>> = mutableMapOf()
            this.callbacks = initialized
            initialized
        }

        return callbacks
            .findOrInit(key) { CallbackImpl<State, Event>(key) }
            .requestAccess {
                duplicateKeyErrorMessage(key)
            } as CallbackImpl<State, Event>
    }

    fun evaluationFinished() {
        callbacks?.clearUnrequested {
            // TODO log that disabled callback was invoked.
            it.disable()
        }
    }

    fun disableAll() {
        callbacks?.forEachValue {
            // TODO log that event is invalid because child was removed
            it.disable()
        }
        callbacks?.clear()
    }
}

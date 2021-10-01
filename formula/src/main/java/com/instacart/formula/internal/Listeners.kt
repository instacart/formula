package com.instacart.formula.internal

internal class Listeners<State> {
    private var listeners: SingleRequestMap<Any, ListenerImpl<State, *>>? = null

    private fun duplicateKeyErrorMessage(key: Any): String {
        if (key is String) {
            // This indicates manual key creation.
            return "Callback $key is already defined. Make sure your key is unique."
        }
        // This indicates automatic key generation
        return "Callback $key is already defined. Are you calling it in a loop or reusing a method? You can wrap the call with FormulaContext.key"
    }

    fun <Event> initOrFindCallback(key: Any): ListenerImpl<State, Event> {
        val listeners = listeners ?: run {
            val initialized: SingleRequestMap<Any, ListenerImpl<State, *>> = mutableMapOf()
            this.listeners = initialized
            initialized
        }

        return listeners
            .findOrInit(key) { ListenerImpl<State, Event>(key) }
            .requestAccess {
                duplicateKeyErrorMessage(key)
            } as ListenerImpl<State, Event>
    }

    fun evaluationFinished() {
        listeners?.clearUnrequested {
            // TODO log that disabled listener was invoked.
            it.disable()
        }
    }

    fun disableAll() {
        listeners?.forEachValue {
            // TODO log that event is invalid because child was removed
            it.disable()
        }
        listeners?.clear()
    }
}

package com.instacart.formula.internal

internal class Listeners {
    private var listeners: SingleRequestMap<Any, ListenerImpl<*, *, *>>? = null

    private fun duplicateKeyErrorMessage(key: Any): String {
        if (key is String) {
            // This indicates manual key creation.
            return "Listener $key is already defined. Make sure your key is unique."
        }
        // This indicates automatic key generation
        return "Listener $key is already defined. Are you calling it in a loop or reusing a method? You can wrap the call with FormulaContext.key"
    }

    fun <Input, State, Event> initOrFindCallback(key: Any): ListenerImpl<Input, State, Event> {
        val listeners = listeners ?: run {
            val initialized: SingleRequestMap<Any, ListenerImpl<*, *, *>> = mutableMapOf()
            this.listeners = initialized
            initialized
        }

        return listeners
            .findOrInit(key) { ListenerImpl<Input, State, Event>(key) }
            .requestAccess {
                duplicateKeyErrorMessage(key)
            } as ListenerImpl<Input, State, Event>
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

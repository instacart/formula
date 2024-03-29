package com.instacart.formula.internal

import java.lang.IllegalStateException

internal class Listeners {
    private var listeners: SingleRequestMap<Any, ListenerImpl<*, *, *>>? = null
    private var indexes: MutableMap<Any, Int>? = null

    private fun duplicateKeyErrorMessage(key: Any): String {
        return "Listener $key is already defined. Unexpected issue."
    }

    fun <Input, State, Event> initOrFindListener(key: Any, useIndex: Boolean): ListenerImpl<Input, State, Event> {
        val currentHolder = listenerHolder<Input, State, Event>(key)
        return if (currentHolder.requested && useIndex) {
            if (key is IndexedKey) {
                // This should never happen, but added as safety
                throw IllegalStateException("Key already indexed (and still duplicate).")
            }

            val index = nextIndex(key)
            val indexedKey = IndexedKey(key, index)
            initOrFindListener(indexedKey, useIndex)
        } else {
            currentHolder
                .requestAccess {
                    duplicateKeyErrorMessage(currentHolder.value.key)
                } as ListenerImpl<Input, State, Event>
        }
    }

    /**
     * After evaluation, we need to clean up temporary state and also disable
     * event listeners that should not be valid anymore.
     */
    fun prepareForPostEvaluation() {
        indexes?.clear()
        listeners?.clearUnrequested(this::disableListener)
    }

    fun disableAll() {
        listeners?.forEachValue {
            // TODO log that event is invalid because child was removed
            it.disable()
        }
        listeners?.clear()
    }

    private fun disableListener(listener: ListenerImpl<*, *, *>) {
        listener.disable()
    }

    /**
     * Function which returns next index for a given key. It will
     * mutate the [indexes] map.
     */
    private fun nextIndex(key: Any): Int {
        val indexes = indexes ?: run {
            val initialized = mutableMapOf<Any, Int>()
            this.indexes = initialized
            initialized
        }

        val index = indexes.getOrElse(key) { 0 } + 1
        indexes[key] = index
        return index
    }

    private fun <Input, State, Event> listenerHolder(key: Any): SingleRequestHolder<ListenerImpl<*, *, *>> {
        val listeners = listeners ?: run {
            val initialized: SingleRequestMap<Any, ListenerImpl<*, *, *>> = mutableMapOf()
            this.listeners = initialized
            initialized
        }

        return listeners.findOrInit(key) { ListenerImpl<Input, State, Event>(key) }
    }
}

package com.instacart.formula.internal

import com.instacart.formula.Transition

internal class Listeners {
    private var listeners: SingleRequestMap<Any, ListenerImpl<*, *, *>>? = null
    private var indexes: MutableMap<Any, Int>? = null

    fun <Input, State, Event> initOrFindListener(
        key: Any,
        useIndex: Boolean,
        transition: Transition<Input, State, Event>
    ): ListenerImpl<Input, State, Event> {
        val currentHolder = listenerHolder(key, transition)
        return if (!currentHolder.requested) {
            currentHolder.requested = true
            currentHolder.value as ListenerImpl<Input, State, Event>
        } else if (useIndex) {
            val index = nextIndex(key)
            val indexedKey = IndexedKey(key, index)
            initOrFindListener(indexedKey, useIndex, transition)
        } else {
            throw IllegalStateException("Listener $key is already defined. Unexpected issue.")
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

        val previousIndex = indexes[key]
        val index = if (previousIndex == null) {
            0
        } else {
            previousIndex + 1
        }
        indexes[key] = index
        return index
    }

    private fun <Input, State, Event> listenerHolder(
        key: Any,
        transition: Transition<Input, State, Event>
    ): SingleRequestHolder<ListenerImpl<*, *, *>> {
        val listeners = listeners ?: run {
            val initialized: SingleRequestMap<Any, ListenerImpl<*, *, *>> = mutableMapOf()
            this.listeners = initialized
            initialized
        }

        return listeners.findOrInit(key) { ListenerImpl(key, transition) }
    }
}

package com.instacart.formula.internal

import com.instacart.formula.Transition

internal class Listeners(private val indexer: Indexer) {
    private var listeners: SingleRequestMap<Any, ListenerImpl<*, *, *>>? = null

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
            val index = indexer.nextIndex(key)
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

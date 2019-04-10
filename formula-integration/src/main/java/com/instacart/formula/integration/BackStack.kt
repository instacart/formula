package com.instacart.formula.integration

/**
 * Represents the current back stack state. It contains a list of
 * screen keys in order where last key in the list is the current screen.
 */
data class BackStack<Key>(val keys: List<Key>) {
    companion object {
        fun <Key> empty(): BackStack<Key> = BackStack(emptyList())
    }

    fun update(event: LifecycleEvent<Key>): BackStack<Key> {
        return when (event) {
            is LifecycleEvent.Added -> add(event.key)
            is LifecycleEvent.Removed -> remove(event.key)
        }
    }

    fun add(key: Key): BackStack<Key> {
        // Only add a contract if it isn't already added.
        return if (keys.contains(key)) {
            this
        } else {
            BackStack(keys.plus(key))
        }
    }

    fun remove(key: Key): BackStack<Key> {
        return if (keys.contains(key)) {
            return BackStack(keys.minus(key))
        } else {
            this
        }
    }
}

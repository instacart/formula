package com.instacart.formula.integration

/**
 * Represents the current back stack state. It contains a list of
 * screen keys in order where last key in the list is the current screen.
 */
data class BackStack<Key>(val keys: List<Key>) {
    companion object {
        val EMPTY = BackStack<Nothing>(emptyList())

        fun <Key> empty(): BackStack<Key> =
            BackStack(emptyList())

        /**
         * Takes last and current active contract state,
         * and calculates attach and detach effects
         */
        fun <Key> findLifecycleEffects(
            lastState: BackStack<Key>?,
            currentState: BackStack<Key>
        ): Set<LifecycleEvent<Key>> {
            val lastActive = lastState?.keys.orEmpty()
            val currentlyActive = currentState.keys

            val attachedEffects = findAttachedKeys(
                lastActive,
                currentlyActive
            )
                .map { LifecycleEvent.Added(it) }

            val detachEffects = findDetachedKeys(
                lastActive,
                currentlyActive
            )
                .map {
                    LifecycleEvent.Removed(it)
                }

            return attachedEffects.plus(detachEffects).toSet()
        }

        fun <Key> findAttachedKeys(lastActive: List<Key>, currentlyActive: List<Key>): List<Key> {
            return currentlyActive.filter { !lastActive.contains(it) }
        }

        fun <Key> findDetachedKeys(lastActive: List<Key>, currentlyActive: List<Key>): List<Key> {
            return lastActive.filter { !currentlyActive.contains(it) }
        }
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

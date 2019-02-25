package com.instacart.formula.integration

/**
 * Stores all active mvi contracts.
 *
 * Null value indicates that store hasn't been initialized.
 */
data class ActiveKeys<Key>(val activeKeys: List<Key>) {
    companion object {
        val EMPTY = ActiveKeys<Nothing>(emptyList())

        fun <Key> empty(): ActiveKeys<Key> =
            ActiveKeys(emptyList())

        /**
         * Takes last and current active contract state,
         * and calculates attach and detach effects
         */
        fun <Key> findLifecycleEffects(
            lastState: ActiveKeys<Key>?,
            currentState: ActiveKeys<Key>
        ): Set<LifecycleEvent<Key>> {
            val lastActive = lastState?.activeKeys.orEmpty()
            val currentlyActive = currentState.activeKeys

            val attachedEffects = findAttachedKeys(
                lastActive,
                currentlyActive
            )
                .map { LifecycleEvent.Attach(it) }

            val detachEffects = findDetachedKeys(
                lastActive,
                currentlyActive
            )
                .map {
                    LifecycleEvent.Detach(it)
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

    fun update(event: LifecycleEvent<Key>): ActiveKeys<Key> {
        return when (event) {
            is LifecycleEvent.Attach -> add(event.key)
            is LifecycleEvent.Detach -> remove(event.key)
        }
    }

    fun add(key: Key): ActiveKeys<Key> {
        // Only add a contract if it isn't already added.
        return if (activeKeys.contains(key)) {
            this
        } else {
            ActiveKeys(activeKeys.plus(key))
        }
    }

    fun remove(key: Key): ActiveKeys<Key> {
        return if (activeKeys.contains(key)) {
            return ActiveKeys(activeKeys.minus(key))
        } else {
            this
        }
    }
}

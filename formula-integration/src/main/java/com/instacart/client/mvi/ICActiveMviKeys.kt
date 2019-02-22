package com.instacart.client.mvi

/**
 * Stores all active mvi contracts.
 *
 * Null value indicates that store hasn't been initialized.
 */
data class ICActiveMviKeys<Key>(val activeKeys: List<Key>) {
    companion object {
        val EMPTY = ICActiveMviKeys<Nothing>(emptyList())

        fun <Key> empty(): ICActiveMviKeys<Key> = ICActiveMviKeys(emptyList())

        /**
         * Takes last and current active contract state,
         * and calculates attach and detach effects
         */
        fun <Key> findLifecycleEffects(
            lastState: ICActiveMviKeys<Key>?,
            currentState: ICActiveMviKeys<Key>
        ): Set<ICMviLifecycleEvent<Key>> {
            val lastActive = lastState?.activeKeys.orEmpty()
            val currentlyActive = currentState.activeKeys

            val attachedEffects = findAttachedKeys(lastActive, currentlyActive)
                .map { ICMviLifecycleEvent.Attach(it) }

            val detachEffects = findDetachedKeys(lastActive, currentlyActive)
                .map {
                    ICMviLifecycleEvent.Detach(it)
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

    fun update(event: ICMviLifecycleEvent<Key>): ICActiveMviKeys<Key> {
        return when (event) {
            is ICMviLifecycleEvent.Attach -> add(event.key)
            is ICMviLifecycleEvent.Detach -> remove(event.key)
        }
    }

    fun add(key: Key): ICActiveMviKeys<Key> {
        // Only add a contract if it isn't already added.
        return if (activeKeys.contains(key)) {
            this
        } else {
            ICActiveMviKeys(activeKeys.plus(key))
        }
    }

    fun remove(key: Key): ICActiveMviKeys<Key> {
        return if (activeKeys.contains(key)) {
            return ICActiveMviKeys(activeKeys.minus(key))
        } else {
            this
        }
    }
}

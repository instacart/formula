package com.instacart.formula.integration.internal

import com.instacart.formula.integration.BackStack
import com.instacart.formula.integration.LifecycleEvent

internal object BackStackUtils {
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

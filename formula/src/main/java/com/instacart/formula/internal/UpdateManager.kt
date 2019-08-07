package com.instacart.formula.internal

import com.instacart.formula.Update

/**
 * Handles [Update] changes.
 */
internal class UpdateManager(
    private val transitionLock: TransitionLock
) {
    companion object {
        val NO_OP: (Any?) -> Unit = {}
    }

    private var updates: LinkedHashSet<Update<*, *>> = LinkedHashSet()

    /**
     * Returns true if there was a transition while updating streams.
     */
    fun updateConnections(new: List<Update<*, *>>, transitionNumber: Long): Boolean {
        if (terminateOld(new, transitionNumber)) {
            return true
        }

        return startNew(new, transitionNumber)
    }

    /**
     * Ensures that all updates will point to the correct listener. Also, disables listeners for
     * terminated streams.
     */
    @Suppress("UNCHECKED_CAST")
    fun updateEventListeners(new: List<Update<*, *>>) {
        updates.forEach { existing ->
            val update = new.firstOrNull { it == existing }
            if (update != null) {
                existing.handler = update.handler as (Any?) -> Unit
            } else {
                existing.handler = NO_OP
            }
        }
    }

    fun terminateOld(new: List<Update<*, *>>, transitionNumber: Long): Boolean {
        val iterator = updates.iterator()
        while (iterator.hasNext()) {
            val existing = iterator.next()

            val update = new.firstOrNull { it == existing }
            if (update == null) {
                iterator.remove()
                existing.handler = NO_OP
                tearDownStream(existing)
            }

            if (transitionLock.hasTransitioned(transitionNumber)) {
                return true
            }
        }
        return false
    }

    fun startNew(new: List<Update<*, *>>, transitionNumber: Long): Boolean {
        new.forEach { update ->
            if (!updates.contains(update)) {
                updates.add(update)
                update.start()

                if (transitionLock.hasTransitioned(transitionNumber)) {
                    return true
                }
            }
        }

        return false
    }

    fun terminate() {
        val iterator = updates.iterator()
        while(iterator.hasNext()) {
            val next = iterator.next()
            iterator.remove()
            tearDownStream(next)
        }
    }

    private fun tearDownStream(existing: Update<*, *>) {
        existing.handler = NO_OP
        existing.tearDown()
    }
}

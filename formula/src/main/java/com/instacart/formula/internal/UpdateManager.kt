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

    /**
     * Returns true if there was a transition while updating streams.
     */
    fun terminateOld(new: List<Update<*, *>>, transitionNumber: Long): Boolean {
        val iterator = updates.iterator()
        while (iterator.hasNext()) {
            val existing = iterator.next()

            val update = new.firstOrNull { it == existing }
            if (update == null) {
                iterator.remove()
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
            val stream = iterator.next()
            iterator.remove()
            tearDownStream(stream)
        }
    }

    private fun tearDownStream(stream: Update<*, *>) {
        stream.tearDown()
        stream.handler = NO_OP
    }
}

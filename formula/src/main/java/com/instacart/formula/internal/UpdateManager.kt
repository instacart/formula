package com.instacart.formula.internal

import com.instacart.formula.Update

/**
 * Handles [Update] changes.
 */
class UpdateManager(
    private val transitionLock: TransitionLock
) {
    companion object {
        val NO_OP: (Any?) -> Unit = {}
    }

    private var updates: LinkedHashSet<Update> = LinkedHashSet()

    /**
     * Returns true if there was a transition while updating streams.
     */
    fun updateConnections(new: List<Update>, transitionNumber: Long): Boolean {
        if (updateExisting(new, transitionNumber)) {
            return true
        }

        return startNew(new, transitionNumber)
    }

    private fun updateExisting(new: List<Update>, transitionNumber: Long): Boolean {
        val iterator = updates.iterator()
        while (iterator.hasNext()) {
            val existing = iterator.next()

            val update = new.firstOrNull { it == existing }
            if (update == null) {
                iterator.remove()

                when (existing) {
                    is Update.Stream<*, *> -> {
                        existing.handler = NO_OP
                        tearDownStream(existing)
                    }
                }
            } else {
                when (existing) {
                    is Update.Stream<*, *> -> {
                        existing.handler = (update as Update.Stream<*, *>).handler as (Any?) -> Unit
                    }
                }
            }

            if (transitionLock.hasTransitioned(transitionNumber)) {
                return true
            }
        }
        return false
    }

    private fun startNew(new: List<Update>, transitionNumber: Long): Boolean {
        new.forEach { update ->
            if (!updates.contains(update)) {
                updates.add(update)
                when (update) {
                    is Update.Stream<*, *> -> update.start()
                    is Update.Effect -> update.action()
                }

                if (transitionLock.hasTransitioned(transitionNumber)) {
                    return true
                }
            }
        }

        return false
    }

    fun terminate() {
        updates.forEach { update ->
            when (update) {
                is Update.Stream<*, *> -> tearDownStream(update)
            }
        }
        updates.clear()
    }

    private fun tearDownStream(existing: Update.Stream<*, *>) {
        existing.handler = NO_OP
        existing.tearDown()
    }
}

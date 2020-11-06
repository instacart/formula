package com.instacart.formula.internal

import com.instacart.formula.Update

/**
 * Handles [Update] changes.
 */
internal class UpdateManager {
    companion object {
        val NO_OP: (Any?) -> Unit = {}
    }

    private var updates: LinkedHashSet<Update<*>>? = null

    /**
     * Ensures that all updates will point to the correct listener. Also, disables listeners for
     * terminated streams.
     */
    @Suppress("UNCHECKED_CAST")
    fun updateEventListeners(new: List<Update<*>>) {
        updates?.forEach { existing ->
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
    fun terminateOld(new: List<Update<*>>, transitionId: TransitionId): Boolean {
        val iterator = updates?.iterator()
        if (iterator != null) {
            while (iterator.hasNext()) {
                val existing = iterator.next()

                if (!shouldKeepRunning(new, existing)) {
                    iterator.remove()
                    tearDownStream(existing)

                    if (transitionId.hasTransitioned()) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun startNew(new: List<Update<*>>, transitionId: TransitionId): Boolean {
        new.forEach { update ->
            val updates = updates ?: run {
                val initialized: LinkedHashSet<Update<*>> = LinkedHashSet()
                updates = initialized
                initialized
            }

            if (!isRunning(update)) {
                updates.add(update)
                update.start()

                if (transitionId.hasTransitioned()) {
                    return true
                }
            }
        }

        return false
    }

    fun terminate() {
        val iterator = updates?.iterator()
        if (iterator != null) {
            while (iterator.hasNext()) {
                val stream = iterator.next()
                iterator.remove()
                tearDownStream(stream)
            }
        }
    }

    private fun shouldKeepRunning(updates: List<Update<*>>, update: Update<*>): Boolean {
        return updates.contains(update)
    }

    private fun isRunning(update: Update<*>): Boolean {
        return updates?.contains(update) ?: false
    }

    private fun tearDownStream(stream: Update<*>) {
        stream.tearDown()
        stream.handler = NO_OP
    }
}

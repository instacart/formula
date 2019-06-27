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

    fun updateConnections(new: List<Update>, transitionNumber: Long) {
        val iterator = updates.iterator()
        while(iterator.hasNext()) {
            val existing = iterator.next()

            val update = new.firstOrNull { it == existing }
            if (update == null) {
                iterator.remove()

                when (existing) {
                    is Update.Stream<*, *> -> {
                        existing.handler = NO_OP
                        existing.tearDown()
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
                return
            }
        }

        new.forEach { update ->
            if (!updates.contains(update)) {
                updates.add(update)
                when (update) {
                    is Update.Stream<*, *> -> update.start()
                    is Update.Effect -> update.action()
                }

                if (transitionLock.hasTransitioned(transitionNumber)) {
                    return
                }
            }
        }
    }

    fun terminate() {
        updates.forEach { update ->
            when(update) {
                is Update.Stream<*, *> -> update.tearDown()
            }
        }
        updates.clear()
    }

}

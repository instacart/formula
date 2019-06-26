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
    private var updates: MutableMap<Update, Update> = mutableMapOf()

    fun updateConnections(new: List<Update>, transitionNumber: Long) {
        val iterator = updates.iterator()
        while(iterator.hasNext()) {
            val existing = iterator.next().key

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
            if (!updates.containsKey(update)) {
                updates[update] = update
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
        updates.values.forEach { update ->
            when(update) {
                is Update.Stream<*, *> -> update.tearDown()
            }
        }
        updates.clear()
    }

}

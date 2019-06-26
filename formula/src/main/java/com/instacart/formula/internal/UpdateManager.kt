package com.instacart.formula.internal

import com.instacart.formula.Update

/**
 * Handles [Update] changes.
 */
class UpdateManager(
    private val processManager: ProcessorManager<*, *, *>
) {
    companion object {
        val NO_OP: (Any?) -> Unit = {}
    }
    private var updates: MutableMap<Update, Update> = mutableMapOf()

    fun updateConnections(new: List<Update>, transitionNumber: Long) {
        updates.forEach { existingUpdate ->
            val existing = existingUpdate.key

            val update = new.firstOrNull { it == existing }
            if (update == null) {
                updates.remove(existing)

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

            if (processManager.hasTransitioned(transitionNumber)) {
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

                if (processManager.hasTransitioned(transitionNumber)) {
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

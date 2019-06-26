package com.instacart.formula.internal

import com.instacart.formula.StreamConnection

class StreamManager(
    private val processManager: ProcessorManager<*, *, *>
) {
    private var updates: MutableMap<UpdateKey, StreamConnection<*, *>> = mutableMapOf()

    fun updateConnections(new: List<StreamConnection<*, *>>, transitionNumber: Long) {
        updates.forEach { existingWorker ->
            val update = new.firstOrNull { it == existingWorker.value }
            if (update == null) {
                updates.remove(existingWorker.key)

                existingWorker.value.handler = {}
                existingWorker.value.tearDown()
            } else {
                existingWorker.value.handler = update.handler as (Any?) -> Unit
            }

            if (processManager.hasTransitioned(transitionNumber)) {
                return
            }
        }

        new.forEach {
            if (!updates.containsKey(it.key)) {
                updates[it.key] = it
                it.start()

                if (processManager.hasTransitioned(transitionNumber)) {
                    return
                }
            }
        }
    }

    fun terminate() {
        updates.values.forEach {
            it.tearDown()
        }
        updates.clear()
    }
}

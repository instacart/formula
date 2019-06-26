package com.instacart.formula.internal

import com.instacart.formula.StreamConnection

class StreamManager(
    private val processManager: ProcessorManager<*, *, *>
) {
    private var updates: MutableMap<UpdateKey, StreamConnection<*, *>> = mutableMapOf()

    fun updateWorkers(workers: List<StreamConnection<*, *>>, transitionNumber: Long) {
        updates.forEach { existingWorker ->
            val update = workers.firstOrNull { it == existingWorker.value }
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

        workers.forEach {
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

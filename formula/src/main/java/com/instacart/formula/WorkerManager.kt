package com.instacart.formula

class WorkerManager(
    private val processManager: ProcessorManager<*, *>
) {
    private var lastWorkers: MutableMap<Worker.Key, Worker<*, *>> = mutableMapOf()

    fun updateWorkers(workers: List<Worker<*, *>>, transitionNumber: Long) {
        lastWorkers.forEach { existingWorker ->
            val update = workers.firstOrNull { it == existingWorker.value }
            if (update == null) {
                lastWorkers.remove(existingWorker.key)

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
            if (!lastWorkers.containsKey(it.key)) {
                lastWorkers[it.key] = it
                it.start()

                if (processManager.hasTransitioned(transitionNumber)) {
                    return
                }
            }
        }
    }

    fun terminate() {
        lastWorkers.values.forEach {
            it.tearDown()
        }
        lastWorkers.clear()
    }
}

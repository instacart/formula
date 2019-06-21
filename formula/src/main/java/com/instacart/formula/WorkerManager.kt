package com.instacart.formula

class WorkerManager {
    private var workers: Map<Worker.Key, Worker<*, *>> = mapOf()

    fun updateWorkers(workers: List<Worker<*, *>>) {
        val updated = mutableMapOf<Worker.Key, Worker<*, *>>()
        this.workers.forEach { existingWorker ->
            val update = workers.firstOrNull { it == existingWorker.value }
            if (update == null) {
                existingWorker.value.handler = {}
                existingWorker.value.tearDown()
            } else {
                existingWorker.value.handler = update.handler as (Any?) -> Unit
                updated[existingWorker.key] = existingWorker.value
            }
        }

        workers.forEach {
            if (!this.workers.containsKey(it.key)) {
                updated[it.key] = it
                it.start()
            }
        }

        this.workers = updated
    }

    fun terminate() {
        workers.values.forEach {
            it.tearDown()
        }
        workers = emptyMap()
    }
}

package com.instacart.formula.batch

import java.util.LinkedList
import java.util.concurrent.atomic.AtomicBoolean

internal class BatchImpl internal constructor(
    private val batchManager: BatchManager,
    private val executor: BatchManager.Executor,
    private val key: Any,
) : BatchScheduler.Batch {

    private val isScheduled = AtomicBoolean(false)
    private val updates = LinkedList<() -> Unit>()

    fun add(update: () -> Unit) {
        updates.add(update)
    }

    override fun execute() {
        /**
         * We do not support batch modifications while processing which is why we removed the
         * batch before processing. If [BatchManager.removeBatch] returns false, that indicates
         * that batch was already processed.
         */
        if (batchManager.removeBatch(this)) {
            executor.executeBatch(updates)
        }
    }

    override fun key(): Any = key

    fun scheduleIfNeeded(batchScheduler: BatchScheduler) {
        if (isScheduled.compareAndSet(false, true)) {
            batchScheduler.schedule(this)
        }
    }
}

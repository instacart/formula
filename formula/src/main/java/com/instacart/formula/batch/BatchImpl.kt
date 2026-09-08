package com.instacart.formula.batch

import java.util.concurrent.atomic.AtomicBoolean

internal class BatchImpl internal constructor(
    private val batchManager: BatchManager,
    private val executor: BatchManager.Executor,
    private val key: Any,
) : BatchScheduler.Batch {

    private companion object {
        // BatchImpl is single-shot — the instance is discarded after execute() — so no shrink
        // logic is needed. Initial capacity sized for the common small-batch case.
        private const val INITIAL_UPDATES_CAPACITY = 4
    }

    private val isScheduled = AtomicBoolean(false)
    private val updates = ArrayDeque<() -> Unit>(INITIAL_UPDATES_CAPACITY)

    fun add(update: () -> Unit) {
        updates.addLast(update)
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

package com.instacart.formula.batch

import com.instacart.formula.FormulaRuntime
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicBoolean

internal class BatchImpl internal constructor(
    private val runtime: FormulaRuntime<*, *>,
    private val batchManager: BatchManager,
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
            runtime.executeBatch {
                for (update in updates) {
                    update()
                }
            }
        }
    }

    override fun key(): Any = key

    fun scheduleIfNeeded(batchScheduler: BatchScheduler) {
        if (isScheduled.compareAndSet(false, true)) {
            batchScheduler.schedule(this)
        }
    }
}

package com.instacart.formula.batch

import com.instacart.formula.FormulaRuntime
import com.instacart.formula.Transition

/**
 * The BatchManager is initialized by [FormulaRuntime] and scoped to [FormulaRuntime] root
 * to manage batched events that happen in that formula tree. When an event marked
 * with [Transition.Batched] arrives:
 * - If we are already processing, we will process the event immediately without batching
 * - Otherwise, we will check if there already exists a batch matching [BatchScheduler.key]. If one
 * exists, we will add the event to that batch.
 * - If no batch exists, we will initialize the batch and call [BatchScheduler.schedule] to
 * schedule the execution of the batch. The [BatchScheduler] will keep a reference to the batch
 * until it is executed.
 */
class BatchManager(private val batchExecutor: Executor) {

    /**
     * Responsible for executing batch of updates.
     *
     * @see [FormulaRuntime]
     */
    interface Executor {
        fun executeBatch(updates: List<() -> Unit>)
    }

    private var activeBatches: MutableMap<Any, BatchImpl>? = null

    internal fun add(batched: Transition.Batched, event: Any?, update: () -> Unit) {
        val batch = synchronized(this) {
            initOrGetBatch(batched.scheduler, event).apply {
                // TODO: implement drop previous event!
                add(update)
            }
        }
        batch.scheduleIfNeeded(batched.scheduler)
    }

    internal fun removeBatch(batch: BatchImpl): Boolean {
        val batchImpl = synchronized(this) {
            activeBatches?.remove(batch.key())
        }
        return batchImpl === batch
    }

    private fun initOrGetBatch(scheduler: BatchScheduler, event: Any?): BatchImpl {
        val activeBatches = activeBatches ?: mutableMapOf()
        this.activeBatches = activeBatches

        val key = scheduler.key(event)
        return activeBatches.getOrPut(key) {
            BatchImpl(this, batchExecutor, key)
        }
    }
}
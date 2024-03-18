package com.instacart.formula.batch

import kotlin.concurrent.getOrSet

/**
 * The StateBatchScheduler class helps batch global state updates that have many
 * subscribers. Without batching, updating global state could cause multiple formula
 * evaluations and produce multiple outputs that need to be processed. To enable
 * state update batching, we need to know when the update is started and finished.
 *
 * An example of global state manager would be
 * ```
 * class MyGlobalStateStore {
 *   private val globalStateRelay = BehaviorRelay.create<GlobalState>()
 *   private val batchScheduler = StateBatchScheduler()
 *
 *   // Expose it, so that it can be used with [Transition.Batched]
 *   fun batchScheduler(): BatchScheduler = batchScheduler
 *
 *   fun publishUpdate(state: GlobalState) {
 *     // Wrap updates in `performUpdate`
 *     batchScheduler.performUpdate { globalStateRelay.accept(state) }
 *   }
 * }
 * ```
 *
 * The main assumption with [StateBatchScheduler] is that formula subscribers are notified on
 * the same thread as [performUpdate] is called. The way batching of state updates works:
 * - We set isUpdating to true
 * - We update our state and notify all the subscribers
 * - The formula subscribers would create a batch and collect all updates within it. Formula
 * calls [StateBatchScheduler.schedule] to add set the batch for execution.
 * - We set isUpdating to false
 * - We execute all the pending batches.
 */
class StateBatchScheduler : BatchScheduler {
    private val isUpdating = ThreadLocal<Boolean>()
    private val threadLocalBatches = ThreadLocal<MutableSet<BatchScheduler.Batch>>()

    fun performUpdate(update: () -> Unit) {
        if (isUpdating.get() == true) {
            // Re-entry!
            update()
            return
        }

        isUpdating.set(true)
        try {
            update()
        } finally {
            isUpdating.remove()

            // Get collected batches
            val batches: MutableSet<BatchScheduler.Batch>? = threadLocalBatches.get()
            // Clear them from our state
            threadLocalBatches.remove()

            // Execute them
            if (batches != null) {
                for(batch in batches) {
                    batch.execute()
                }
            }
        }
    }

    override fun schedule(batch: BatchScheduler.Batch) {
        if (isUpdating.get() == true) {
            val batches = threadLocalBatches.getOrSet { mutableSetOf() }
            batches.add(batch)
        } else {
            // No active update, let's just execute it.
            batch.execute()
        }
    }

    override fun key(event: Any?): Any {
        /**
         * We use current thread as part of the batch key because one of the assumptions
         * are that subscriber initializes the batch on the same thread as the update.
         */
        return Pair(Thread.currentThread(), this)
    }
}
package com.instacart.formula.types

import com.instacart.formula.batch.BatchScheduler
import com.instacart.formula.batch.StateBatchScheduler

class TestStateBatchScheduler : BatchScheduler {
    private val delegate = StateBatchScheduler()

    private val activeUpdates = mutableSetOf<Any>()

    val batchesOutsideOfScope = mutableListOf<BatchScheduler.Batch>()

    fun performUpdate(event: Any?, update: () -> Unit) {
        val key = key(event)
        activeUpdates.add(key)
        delegate.performUpdate(update)
        activeUpdates.remove(key)
    }

    override fun schedule(batch: BatchScheduler.Batch) {
        val key = batch.key()
        if (!activeUpdates.contains(key)) {
            batchesOutsideOfScope.add(batch)
        }

        delegate.schedule(batch)
    }

    override fun key(event: Any?): Any {
        return delegate.key(event)
    }
}
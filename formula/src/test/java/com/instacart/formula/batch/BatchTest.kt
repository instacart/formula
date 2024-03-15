package com.instacart.formula.batch

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Transition
import com.instacart.formula.test.TestCallback
import org.junit.Test

class BatchTest {

    object SimpleBatchExecutor : BatchManager.Executor {
        override fun executeBatch(updates: List<() -> Unit>) {
            for (update in updates) {
                update()
            }
        }
    }

    class SimpleBatchScheduler(
        private val scheduledBatches: MutableList<BatchScheduler.Batch>,
    ) : BatchScheduler {
        override fun schedule(batch: BatchScheduler.Batch) {
            scheduledBatches.add(batch)
        }
    }

    @Test
    fun `only one batch per key`() {
        val scheduledBatches = mutableListOf<BatchScheduler.Batch>()
        val batchScheduler = SimpleBatchScheduler(scheduledBatches)
        val batchedType = Transition.Batched(batchScheduler)
        val testAction = TestCallback()

        val batchManager = BatchManager(SimpleBatchExecutor)
        batchManager.add(batchedType, Unit, testAction)
        batchManager.add(batchedType, Unit, testAction)
        batchManager.add(batchedType, Unit, testAction)

        // Only one batch
        assertThat(scheduledBatches).hasSize(1)

        val otherScheduler = SimpleBatchScheduler(scheduledBatches)
        val otherBatch = Transition.Batched(otherScheduler)
        batchManager.add(otherBatch, Unit, testAction)

        // Two batches now since other scheduler was used
        assertThat(scheduledBatches).hasSize(2)
    }

    @Test fun `can execute same batch only once`() {
        val scheduledBatches = mutableListOf<BatchScheduler.Batch>()
        val batchScheduler = SimpleBatchScheduler(scheduledBatches)
        val batchedType = Transition.Batched(batchScheduler)
        val testAction = TestCallback()

        val batchManager = BatchManager(SimpleBatchExecutor)
        batchManager.add(batchedType, Unit, testAction)
        batchManager.add(batchedType, Unit, testAction)
        batchManager.add(batchedType, Unit, testAction)


        for (batch in scheduledBatches) {
            batch.execute()
            batch.execute()
            batch.execute()
            batch.execute()
            batch.execute()
        }

        // Only three actions - each executed once
        testAction.assertTimesCalled(3)
    }

    @Test fun `remove batch returns false if not initialized`() {
        val batchManager = BatchManager(SimpleBatchExecutor)
        val batch = BatchImpl(batchManager, SimpleBatchExecutor, Unit)
        val removed = batchManager.removeBatch(batch)
        assertThat(removed).isFalse()
    }

    @Test fun `state batch scheduler will execute batch if no update is in progress`() {
        val batchScheduler = StateBatchScheduler()
        val batchManager = BatchManager(SimpleBatchExecutor)
        val testAction = TestCallback()

        val executionType = Transition.Batched(batchScheduler)
        batchManager.add(executionType, Unit, testAction)
        batchManager.add(executionType, Unit, testAction)

        testAction.assertTimesCalled(2)
    }

    @Test fun `state batch scheduler clears batches from previous run`() {
        var executed = 0
        val batch = object : BatchScheduler.Batch {
            override fun execute() {
                executed += 1
            }

            override fun key(): Any = Unit
        }

        val batchScheduler = StateBatchScheduler()
        batchScheduler.performUpdate {
            batchScheduler.schedule(batch)
        }

        // Single execution
        assertThat(executed).isEqualTo(1)

        // No batches scheduled
        batchScheduler.performUpdate {  }

        // No new updates
        assertThat(executed).isEqualTo(1)
    }

    @Test fun `state manager runs re-entrant update immediately`() {
        val order = mutableListOf<Int>()
        val batchScheduler = StateBatchScheduler()
        batchScheduler.performUpdate {
            order.add(1)
            batchScheduler.performUpdate {
                order.add(2)
            }
            order.add(3)
        }

        assertThat(order).containsExactly(1, 2, 3).inOrder()
    }
}
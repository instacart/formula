package com.instacart.formula.subjects

import com.google.common.truth.Truth
import com.instacart.formula.subjects.SleepFormula.SleepEvent
import com.instacart.formula.test.test
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

class MultiThreadRobot() {
    class NamedThreadFactory(private val name: String): ThreadFactory {
        override fun newThread(r: Runnable): Thread {
            return Thread(r, name)
        }
    }

    private val threadFormula = SleepFormula()
    private val observer = threadFormula.test().input("initial-key")

    // Manage executors and update completion
    private val executorMap = mutableMapOf<String, Executor>()
    private val eventCompletionLatches = ConcurrentLinkedQueue<CountDownLatch>()

    fun thread(name: String, sleepDuration: Long) = apply {
        thread(name) {
           observer.output {
               this.onSleep(sleepDuration)
           }
        }
    }

    fun thread(name: String, function: () -> Unit) = apply {
        val executor = executorMap.getOrPut(name) {
            Executors.newSingleThreadExecutor(NamedThreadFactory(name))
        }

        // Creating a latch and adding it to a list to make sure we are able to
        // wait for all event completion.
        val completionLatch = CountDownLatch(1)
        eventCompletionLatches.add(completionLatch)

        executor.execute {
            observer.output {
                function()
                completionLatch.countDown()
            }
        }
    }

    fun input(newKey: String) = apply {
        observer.input(newKey)
    }

    fun dispose() = apply {
        observer.dispose()
    }

    fun awaitCompletion() = apply {
        for (latch in eventCompletionLatches) {
            await(latch, 1, TimeUnit.SECONDS)
        }
    }

    fun awaitEvents(vararg sleepEvents: SleepEvent) = apply {
        awaitCompletion()

        observer.output {
            Truth.assertThat(this.sleepEvents).containsExactly(*sleepEvents).inOrder()
        }
    }

    fun awaitEvents(assertEvents: (List<SleepEvent>) -> Unit) = apply {
        awaitCompletion()
        observer.output {
            assertEvents(sleepEvents)
        }
    }

    private fun await(latch: CountDownLatch, timeout: Long, unit: TimeUnit) {
        if (!latch.await(timeout, unit)) {
            throw IllegalStateException("Timeout")
        }
    }
}
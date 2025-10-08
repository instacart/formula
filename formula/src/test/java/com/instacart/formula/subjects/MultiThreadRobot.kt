package com.instacart.formula.subjects

import com.google.common.truth.Truth
import com.instacart.formula.subjects.SleepFormula.SleepEvent
import com.instacart.formula.test.test
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

    // Manage executors
    private val executorMap = mutableMapOf<String, Executor>()
    private var expectedEventCount = 0

    fun thread(name: String, sleepDuration: Long) = apply {
        expectedEventCount++
        val executor = executorMap.getOrPut(name) {
            Executors.newSingleThreadExecutor(NamedThreadFactory(name))
        }

        executor.execute {
            observer.output {
                this.onSleep(sleepDuration)
            }
        }
    }

    fun thread(name: String, function: () -> Unit) = apply {
        val executor = executorMap.getOrPut(name) {
            Executors.newSingleThreadExecutor(NamedThreadFactory(name))
        }

        executor.execute {
            observer.output {
                function()
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
        // Poll until all expected events have been processed (Actions completed)
        val deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5)
        while (System.currentTimeMillis() < deadline) {
            val currentCount = observer.values().lastOrNull()?.sleepEvents?.size ?: 0
            if (currentCount >= expectedEventCount) {
                // Give a small delay to ensure the SynchronizedUpdateQueue is fully idle
                Thread.sleep(10)
                return@apply
            }
            Thread.sleep(10)
        }
        throw IllegalStateException("Timeout waiting for $expectedEventCount events, got ${observer.values().lastOrNull()?.sleepEvents?.size ?: 0}")
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
}
package com.instacart.formula.subjects

import com.google.common.truth.Truth
import com.instacart.formula.plugin.Inspector
import com.instacart.formula.subjects.SleepFormula.SleepEvent
import com.instacart.formula.test.test
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

class MultiThreadRobot(scope: CoroutineScope) {
    class NamedThreadFactory(private val name: String): ThreadFactory {
        override fun newThread(r: Runnable): Thread {
            return Thread(r, name)
        }
    }

    // Latches that fire when the named thread's onSleep transition is observed by the
    // inspector. The inspector callback runs synchronously inside SynchronizedUpdateQueue's
    // takeOver, so a countdown proves threadRunning == that thread.
    private val takeOverLatches = ConcurrentHashMap<String, CountDownLatch>()

    private val takeOverInspector = object : Inspector {
        override fun onStateChanged(formulaType: Class<*>, event: Any?, old: Any?, new: Any?) {
            val pending = (new as? SleepFormula.State)?.pendingEvent ?: return
            takeOverLatches.remove(pending.threadName)?.countDown()
        }
    }

    private val threadFormula = SleepFormula()
    private val observer = threadFormula.test(scope, inspector = takeOverInspector).input("initial-key")

    // Manage executors
    private val executorMap = mutableMapOf<String, Executor>()
    private var expectedEventCount = 0

    /**
     * Submits a sleep event on [name].
     *
     * Set [awaitTakeOver] to true to block until this thread has actually claimed the
     * SynchronizedUpdateQueue — useful for tests that need a deterministic ordering of
     * which thread becomes the executing thread before another thread enqueues work.
     */
    fun thread(name: String, sleepDuration: Long, awaitTakeOver: Boolean = false) = apply {
        expectedEventCount++
        val executor = executorMap.getOrPut(name) {
            Executors.newSingleThreadExecutor(NamedThreadFactory(name))
        }

        val latch = if (awaitTakeOver) {
            CountDownLatch(1).also { takeOverLatches[name] = it }
        } else {
            null
        }

        executor.execute {
            observer.output {
                this.onSleep(sleepDuration)
            }
        }

        if (latch != null && !latch.await(5, TimeUnit.SECONDS)) {
            takeOverLatches.remove(name)
            throw IllegalStateException("Timeout waiting for thread $name to take over the queue")
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

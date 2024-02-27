package com.instacart.formula.internal

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference

/**
 * We can only process one formula update at a time. To enable thread-safety we use a
 * non-blocking event queue with a serial confinement strategy for queue processing. All external
 * formula events use [postUpdate] function which adds the update to [updateQueue], and then,
 * tries to start processing the queue via atomic [threadRunning] variable. If another thread
 * was first to take over [threadRunning], we let that thread continue and we exit out. Given
 * all formula state access is gated via [threadRunning] atomic reference, we are able to ensure
 * that there is happens-before relationship between each thread and memory changes are visible
 * between them.
 */
class SynchronizedUpdateQueue(
    private val onEmpty: (() -> Unit)? = null,
) {
    /**
     * Defines a thread currently executing formula update. Null value indicates idle queue.
     *
     * To ensure that memory changes within formula internals are synchronized between threads,
     * we piggyback on the internal synchronization of this variable. Modification to this
     * variable wraps around every formula update:
     * - threadRunning = MyThread
     * - formulaUpdate()
     * - threadRunning = null
     *
     * This creates happens-before relationship between multiple threads and makes sure that
     * all modifications within formulaUpdate() block are visible to the next thread.
     */
    private val threadRunning = AtomicReference<Thread>()

    /**
     * A non-blocking thread-safe FIFO queue that tracks pending updates.
     */
    private val updateQueue = ConcurrentLinkedQueue<() -> Unit>()

    /**
     * To ensure that we execute one update at a time, all external formula events use this
     * function to post updates. We add the update to a queue and then try to start processing.
     * Failure to start processing indicates that another thread was first and we allow that
     * thread to continue.
     */
    fun postUpdate(update: () -> Unit) {
        val currentThread = Thread.currentThread()
        val owner = threadRunning.get()
        if (owner == currentThread) {
            // This indicates a nested update where an update triggers another update. Given we
            // are already thread gated, we can execute this update immediately without a need
            // for any extra synchronization.
            update()
            return
        }

        val updateExecuted = if (updateQueue.peek() == null) {
            // No pending update, let's try to run our update immediately
            takeOver(currentThread, update)
        } else {
            false
        }

        if (!updateExecuted) {
            updateQueue.add(update)
        }
        tryToDrainQueue(currentThread)
    }

    /**
     * Tries to drain the update queue. It will process one update at a time until
     * queue is empty or another thread takes over processing.
     */
    private fun tryToDrainQueue(currentThread: Thread) {
        while (true) {
            // First, we peek to see if there is a value to process.
            val peekUpdate = updateQueue.peek()
            if (peekUpdate != null) {
                // Since there is a pending update, we try to process it.
                val updateExecuted = takeOver(currentThread, this::pollAndExecute)
                if (!updateExecuted) {
                    return
                }
            } else {
                onEmpty?.invoke()
                return
            }
        }
    }

    private fun pollAndExecute() {
        // We remove first update from the queue and execute if it exists.
        val actualUpdate = updateQueue.poll()
        actualUpdate?.invoke()
    }

    /**
     * Tries to take over the processing and execute an [update].
     *
     * Returns true if it was able to successfully claim the ownership and execute the
     * update. Otherwise, returns false (this indicates another thread claimed the right first).
     */
    private fun takeOver(currentThread: Thread, update: () -> Unit): Boolean {
        return if (threadRunning.compareAndSet(null, currentThread)) {
            // We took over the processing, let's execute the [update]
            try {
                update()
            } finally {
                // We reset the running thread. To ensure happens-before relationship, this must
                // always happen after the [update].
                threadRunning.set(null)
            }
            true
        } else {
            // Another thread is running, so we return false.
            false
        }
    }
}
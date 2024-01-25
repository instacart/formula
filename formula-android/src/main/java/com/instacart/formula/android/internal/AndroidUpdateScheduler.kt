package com.instacart.formula.android.internal

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Handles state update scheduling to the main thread. If update arrives on a background thread,
 * it will added it the main thread queue. It will throw away a pending update if a new update
 * arrives.
 */
class AndroidUpdateScheduler<Value : Any>(
    private val update: (Value) -> Unit,
) {
    /**
     * If not null, that means that we have an update pending.
     */
    private val pendingValue = AtomicReference<Value>()

    /**
     * Defines if an update is currently scheduled.
     */
    private val updateScheduled = AtomicBoolean(false)

    /**
     * To avoid re-entry, we track if [updateRunnable] is currently handling an update.
     */
    private var isUpdating = false

    private val updateRunnable = object : Runnable {
        override fun run() {
            var localPending = pendingValue.getAndSet(null)
            while (localPending != null) {
                updateScheduled.set(false)

                // Handle the update
                isUpdating = true
                update(localPending)
                isUpdating = false

                // Check if another update arrived while we were processing.
                localPending = pendingValue.getAndSet(null)

                if (localPending != null) {
                    // We will take over processing, so let's clear the message
                    Utils.mainThreadHandler.removeCallbacks(this)
                }
            }
        }
    }

    fun emitUpdate(value: Value) {
        // Set pending value
        pendingValue.set(value)

        if (Utils.isMainThread()) {
            if (isUpdating) {
                // Let's exit and let the [updateRunnable] to pick up the change
                return
            } else {
                // Since we are on main thread, let's force run it
                updateRunnable.run()
            }
        } else {
            // If no update is scheduled, schedule one
            if (updateScheduled.compareAndSet(false, true)) {
                Utils.mainThreadHandler.post(updateRunnable)
            }
        }
    }
}
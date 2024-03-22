package com.instacart.formula.android.internal

import android.os.Build
import android.os.Message
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Handles state update scheduling to the main thread. If an update arrives on a background thread,
 * it will be set to pending and executed when the main thread is ready. This class will throw
 * away a pending update if a new update arrives.
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
            updateScheduled.set(false)

            var localPending = pendingValue.getAndSet(null)
            while (localPending != null) {
                // Handle the update
                isUpdating = true
                update(localPending)
                isUpdating = false

                // Check if another update arrived while we were processing.
                localPending = pendingValue.getAndSet(null)

                if (localPending != null) {
                    // We will perform the update, let's clear the values.
                    updateScheduled.set(false)
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
                val message = Message.obtain(Utils.mainThreadHandler, updateRunnable)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    message.isAsynchronous = true
                }
                Utils.mainThreadHandler.sendMessage(message)
            }
        }
    }
}
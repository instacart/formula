package com.instacart.formula.internal

import com.instacart.formula.BoundStream

/**
 * Handles [BoundStream] changes.
 */
internal class UpdateManager {
    companion object {
        val NO_OP: (Any?) -> Unit = {}
    }

    private var running: LinkedHashSet<BoundStream<*>>? = null

    /**
     * Ensures that all updates will point to the correct listener. Also, disables listeners for
     * terminated streams.
     */
    @Suppress("UNCHECKED_CAST")
    fun updateEventListeners(new: List<BoundStream<*>>) {
        running?.forEach { existing ->
            val update = new.firstOrNull { it == existing }
            if (update != null) {
                existing.handler = update.handler as (Any?) -> Unit
            } else {
                existing.handler = NO_OP
            }
        }
    }

    /**
     * Returns true if there was a transition while terminating streams.
     */
    fun terminateOld(requested: List<BoundStream<*>>, transitionId: TransitionId): Boolean {
        val iterator = running?.iterator() ?: return false
        while (iterator.hasNext()) {
            val running = iterator.next()

            if (!shouldKeepRunning(requested, running)) {
                iterator.remove()
                tearDownStream(running)

                if (transitionId.hasTransitioned()) {
                    return true
                }
            }
        }
        return false
    }

    fun startNew(requested: List<BoundStream<*>>, transitionId: TransitionId): Boolean {
        for (update in requested) {
            val running = getOrInitRunningStreamList()
            if (!isRunning(update)) {
                running.add(update)
                update.start()

                if (transitionId.hasTransitioned()) {
                    return true
                }
            }
        }

        return false
    }

    fun terminate() {
        val running = running ?: return
        this.running = null
        for (update in running) {
            tearDownStream(update)
        }
    }

    private fun shouldKeepRunning(updates: List<BoundStream<*>>, update: BoundStream<*>): Boolean {
        return updates.contains(update)
    }

    private fun isRunning(update: BoundStream<*>): Boolean {
        return running?.contains(update) ?: false
    }

    private fun tearDownStream(stream: BoundStream<*>) {
        stream.tearDown()
        stream.handler = NO_OP
    }

    private fun getOrInitRunningStreamList(): LinkedHashSet<BoundStream<*>> {
        return running ?: run {
            val initialized: LinkedHashSet<BoundStream<*>> = LinkedHashSet()
            this.running = initialized
            initialized
        }
    }
}

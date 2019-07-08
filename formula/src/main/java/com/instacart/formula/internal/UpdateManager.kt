package com.instacart.formula.internal

import com.instacart.formula.Update

/**
 * Handles [Update] changes.
 */
internal class UpdateManager(private val logger: FormulaLogger) {
    companion object {
        val NO_OP: (Any?) -> Unit = {}
    }

    private var running: LinkedHashSet<Update<*>>? = null

    /**
     * Ensures that all updates will point to the correct listener. Also, disables listeners for
     * terminated streams.
     */
    @Suppress("UNCHECKED_CAST")
    fun updateEventListeners(new: List<Update<*>>) {
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
    fun terminateOld(requested: List<Update<*>>, transitionId: TransitionId): Boolean {
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

    fun startNew(requested: List<Update<*>>, transitionId: TransitionId): Boolean {
        for (update in requested) {
            val running = getOrInitRunningStreamList()
            if (!isRunning(update)) {
                running.add(update)
                logger.log { "stream:start ${update.toDisplayName()}" }
                update.start()

                if (transitionId.hasTransitioned()) {
                    return true
                }
            } else {
                logger.log { "stream:skip (already running ${update.toDisplayName()})" }
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

    private fun shouldKeepRunning(updates: List<Update<*>>, update: Update<*>): Boolean {
        return updates.contains(update)
    }

    private fun isRunning(update: Update<*>): Boolean {
        return running?.contains(update) ?: false
    }

    private fun tearDownStream(stream: Update<*>) {
        logger.log { "stream:terminate ${stream.toDisplayName()}" }
        stream.tearDown()
        stream.handler = NO_OP
    }

    private fun getOrInitRunningStreamList(): LinkedHashSet<Update<*>> {
        return running ?: run {
            val initialized: LinkedHashSet<Update<*>> = LinkedHashSet()
            this.running = initialized
            initialized
        }
    }

    /**
     * Generates a display name to be used within logs.
     */
    private fun Update<*>.toDisplayName(): String {
        return stream.javaClass.name
            .replace("$$", "-")
            .replace('$', '-')
    }
}

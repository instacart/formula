package com.instacart.formula.internal

import com.instacart.formula.DeferredAction
import com.instacart.formula.Inspector
import kotlin.reflect.KClass

/**
 * Handles [DeferredAction] changes.
 */
internal class ActionManager(
    private val formulaType: KClass<*>,
    private val inspector: Inspector?,
) {
    companion object {
        val NO_OP: (Any?) -> Unit = {}
    }

    private var running: LinkedHashSet<DeferredAction<*>>? = null

    /**
     * Ensures that all updates will point to the correct listener. Also, disables listeners for
     * terminated streams.
     */
    @Suppress("UNCHECKED_CAST")
    fun updateEventListeners(new: List<DeferredAction<*>>) {
        running?.forEach { existing ->
            val update = new.firstOrNull { it == existing }
            if (update != null) {
                existing.listener = update.listener as (Any?) -> Unit
            } else {
                existing.listener = NO_OP
            }
        }
    }

    /**
     * Returns true if there was a transition while terminating streams.
     */
    fun terminateOld(requested: List<DeferredAction<*>>, transitionId: TransitionId): Boolean {
        val iterator = running?.iterator() ?: return false
        while (iterator.hasNext()) {
            val running = iterator.next()

            if (!shouldKeepRunning(requested, running)) {
                iterator.remove()
                finishAction(running)

                if (transitionId.hasTransitioned()) {
                    return true
                }
            }
        }
        return false
    }

    fun startNew(requested: List<DeferredAction<*>>, transitionId: TransitionId): Boolean {
        for (action in requested) {
            val running = getOrInitRunningStreamList()
            if (!isRunning(action)) {
                inspector?.onActionStarted(formulaType, action)

                running.add(action)
                action.start()

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
            finishAction(update)
        }
    }

    private fun shouldKeepRunning(updates: List<DeferredAction<*>>, update: DeferredAction<*>): Boolean {
        return updates.contains(update)
    }

    private fun isRunning(update: DeferredAction<*>): Boolean {
        return running?.contains(update) ?: false
    }

    private fun finishAction(action: DeferredAction<*>) {
        inspector?.onActionFinished(formulaType, action)
        action.tearDown()
        action.listener = NO_OP
    }

    private fun getOrInitRunningStreamList(): LinkedHashSet<DeferredAction<*>> {
        return running ?: run {
            val initialized: LinkedHashSet<DeferredAction<*>> = LinkedHashSet()
            this.running = initialized
            initialized
        }
    }
}

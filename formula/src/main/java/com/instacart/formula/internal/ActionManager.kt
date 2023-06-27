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
    private var actions: Collection<DeferredAction<*>>? = null

    private var startListInvalidated: Boolean = false
    private var scheduledToStart: MutableList<DeferredAction<*>>? = null

    private var removeListInvalidated: Boolean = false
    private var scheduledForRemoval: MutableList<DeferredAction<*>>? = null

    fun onNewFrame(new: Collection<DeferredAction<*>>) {
        actions = new
        startListInvalidated = true
        removeListInvalidated = true
    }

    /**
     * Returns true if there was a transition while terminating streams.
     */
    fun terminateOld(transitionId: TransitionId): Boolean {
        prepareStoppedActionList()

        val scheduled = scheduledForRemoval ?: return false
        val iterator = scheduled.iterator()
        while (iterator.hasNext()) {
            val action = iterator.next()
            iterator.remove()

            running?.remove(action)
            finishAction(action)

            if (transitionId.hasTransitioned()) {
                return true
            }
        }
        return false
    }

    fun startNew(transitionId: TransitionId): Boolean {
        prepareNewActionList()

        val scheduled = scheduledToStart ?: return false
        val iterator = scheduled.iterator()
        while (iterator.hasNext()) {
            val action = iterator.next()
            iterator.remove()

            inspector?.onActionStarted(formulaType, action)

            getOrInitRunningActions().add(action)
            action.start()

            if (transitionId.hasTransitioned()) {
                return true
            }
        }

        return false
    }

    fun terminate() {
        val running = running ?: return
        this.running = null
        for (action in running) {
            finishAction(action)
        }
    }

    private fun prepareNewActionList() {
        if (!startListInvalidated) {
            return
        }

        startListInvalidated = false
        scheduledToStart?.clear()

        val actionList = actions
        if (!actionList.isNullOrEmpty()) {
            for (action in actionList) {
                if (!isRunning(action)) {
                    val list = scheduledToStart ?: mutableListOf<DeferredAction<*>>().apply {
                        scheduledToStart = this
                    }
                    list.add(action)
                }
            }
        }
    }

    private fun prepareStoppedActionList() {
        if (!removeListInvalidated) {
            return
        }
        removeListInvalidated = false
        scheduledForRemoval?.clear()

        val running = running
        if (running != null) {
            for (action in running) {
                val actions = actions ?: emptyList()
                if (!actions.contains(action)) {
                    val list = scheduledForRemoval ?: mutableListOf<DeferredAction<*>>().apply {
                        scheduledForRemoval = this
                    }
                    list.add(action)
                }
            }
        }
    }

    private fun isRunning(update: DeferredAction<*>): Boolean {
        return running?.contains(update) ?: false
    }

    private fun finishAction(action: DeferredAction<*>) {
        inspector?.onActionFinished(formulaType, action)
        action.tearDown()
        action.listener = NO_OP
    }

    private fun getOrInitRunningActions(): LinkedHashSet<DeferredAction<*>> {
        return running ?: run {
            val initialized: LinkedHashSet<DeferredAction<*>> = LinkedHashSet()
            this.running = initialized
            initialized
        }
    }
}

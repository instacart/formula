package com.instacart.formula.internal

import com.instacart.formula.DeferredAction
import com.instacart.formula.Inspector
import kotlin.reflect.KClass

/**
 * Handles [DeferredAction] changes.
 */
internal class ActionManager(
    private val manager: FormulaManagerImpl<*, *, *>,
    private val formulaType: KClass<*>,
    private val inspector: Inspector?,
) {
    companion object {
        val NO_OP: (Any?) -> Unit = {}
    }

    private var running: LinkedHashSet<DeferredAction<*>>? = null
    private var actions: Set<DeferredAction<*>>? = null

    private var startListInvalidated: Boolean = false
    private var scheduledToStart: MutableList<DeferredAction<*>>? = null

    private var removeListInvalidated: Boolean = false
    private var scheduledForRemoval: MutableList<DeferredAction<*>>? = null

    fun onNewFrame(new: Set<DeferredAction<*>>) {
        actions = new

        startListInvalidated = true
        removeListInvalidated = true
    }

    /**
     * Returns true if there was a transition while terminating streams.
     */
    fun terminateOld(transitionID: Long): Boolean {
        prepareStoppedActionList()

        if (scheduledForRemoval.isNullOrEmpty()) {
            return false
        }

        val actions = actions ?: emptyList()
        val iterator = scheduledForRemoval?.iterator()
        while (iterator?.hasNext() == true) {
            val action = iterator.next()
            iterator.remove()

            if (!actions.contains(action)) {
                running?.remove(action)
                finishAction(action)

                if (manager.hasTransitioned(transitionID)) {
                    return true
                }
            }
        }
        return false
    }

    fun startNew(transitionID: Long): Boolean {
        prepareNewActionList()

        val scheduled = scheduledToStart ?: return false
        if (scheduled.isEmpty()) {
            return false
        }

        val iterator = scheduled.iterator()
        while (iterator.hasNext()) {
            val action = iterator.next()
            iterator.remove()

            if (!isRunning(action)) {
                inspector?.onActionStarted(formulaType, action)

                getOrInitRunningActions().add(action)
                action.start()

                if (manager.hasTransitioned(transitionID)) {
                    return true
                }
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

        val actionList = actions ?: emptyList()
        if (!actionList.isEmpty()) {
            if (scheduledToStart == null) {
                scheduledToStart = mutableListOf()
            }
            scheduledToStart?.addAll(actionList)
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
        if (!running.isNullOrEmpty()) {
            if (scheduledForRemoval == null) {
                scheduledForRemoval = mutableListOf()
            }

            scheduledForRemoval?.addAll(running ?: emptyList())
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

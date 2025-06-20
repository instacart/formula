package com.instacart.formula.internal

import com.instacart.formula.DeferredAction
import com.instacart.formula.Evaluation
import com.instacart.formula.plugin.Inspector
import kotlinx.coroutines.isActive

/**
 * Handles [DeferredAction] changes.
 */
internal class ActionManager(
    private val manager: FormulaManagerImpl<*, *, *>,
    private val inspector: Inspector?,
) {
    /**
     * Currently running actions
     */
    private var running: LinkedHashSet<DeferredAction<*>>? = null

    /**
     * Action list provided by [Evaluation.actions]
     */
    private var actions: Set<DeferredAction<*>> = emptySet()

    private var recomputeCheckToStartList: Boolean = false
    private var checkToStartActionList: MutableList<DeferredAction<*>>? = null

    private var recomputeCheckToRemoveList: Boolean = false
    private var checkToRemoveActionList: MutableList<DeferredAction<*>>? = null

    /**
     * After evaluation, we might have a new list of actions that we need
     * to start and some old ones that will need to be terminates. This function
     * prepares for that work which will be performed in [terminateOld] and [startNew].
     */
    fun prepareForPostEvaluation(new: Set<DeferredAction<*>>) {
        actions = new

        recomputeCheckToStartList = true
        recomputeCheckToRemoveList = true
    }

    /**
     * Returns true if there was a transition while terminating streams.
     */
    fun terminateOld(evaluationId: Long): Boolean {
        recomputeCheckToRemoveActionListIfNeeded()

        val runningActionList = running ?: return false
        val scheduled = checkToRemoveActionList?.takeIf { it.isNotEmpty() } ?: return false

        val iterator = scheduled.iterator()
        while (iterator.hasNext()) {
            val action = iterator.next()
            iterator.remove()

            if (!actions.contains(action)) {
                runningActionList.remove(action)
                finishAction(action)

                if (manager.isTerminated()) {
                    return false
                }

                if (!manager.canUpdatesContinue(evaluationId)) {
                    return true
                }
            }
        }
        return false
    }

    fun startNew(evaluationId: Long): Boolean {
        recomputeCheckToStartActionListIfNeeded()

        val scheduled = checkToStartActionList?.takeIf { it.isNotEmpty() } ?: return false

        val iterator = scheduled.iterator()
        while (iterator.hasNext()) {
            val action = iterator.next()
            iterator.remove()

            if (!manager.scope.isActive) {
                // Cannot start any new actions if coroutine scope is not active anymore.
                return false
            }

            val runningActions = getOrInitRunningActions()
            if (!runningActions.contains(action)) {
                inspector?.onActionStarted(manager.formulaType, action)

                runningActions.add(action)
                action.start(manager.scope, manager.formulaType.java)

                if (manager.isTerminated()) {
                    return false
                }

                if (!manager.canUpdatesContinue(evaluationId)) {
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

    private fun recomputeCheckToStartActionListIfNeeded() {
        if (recomputeCheckToStartList) {
            recomputeCheckToStartList = false

            checkToStartActionList?.clear()
            val list = checkToStartActionList
            if (actions.isEmpty()) {
                list?.clear()
            } else if (list != null) {
                list.clear()
                list.addAll(actions)
            } else {
                checkToStartActionList = ArrayList(actions)
            }
        }
    }

    private fun recomputeCheckToRemoveActionListIfNeeded() {
        if (recomputeCheckToRemoveList) {
            recomputeCheckToRemoveList = false

            val list = checkToRemoveActionList
            val runningList = running?.takeIf { it.isNotEmpty() }
            if (runningList == null) {
                list?.clear()
            } else if (list != null) {
                list.clear()
                list.addAll(runningList)
            } else {
                checkToRemoveActionList = ArrayList(runningList)
            }
        }
    }

    private fun finishAction(action: DeferredAction<*>) {
        inspector?.onActionFinished(manager.formulaType, action)
        action.tearDown()
        action.listener = null
    }

    private fun getOrInitRunningActions(): LinkedHashSet<DeferredAction<*>> {
        return running ?: run {
            val initialized: LinkedHashSet<DeferredAction<*>> = LinkedHashSet()
            this.running = initialized
            initialized
        }
    }
}

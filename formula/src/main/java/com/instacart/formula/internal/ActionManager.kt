package com.instacart.formula.internal

import com.instacart.formula.Action
import com.instacart.formula.DeferredAction
import com.instacart.formula.Listener
import com.instacart.formula.plugin.Inspector
import com.instacart.formula.validation.ActionValidationFrame
import kotlinx.coroutines.isActive

/**
 * Handles [DeferredAction] changes.
 */
internal class ActionManager(
    private val manager: FormulaManagerImpl<*, *, *>,
    private val inspector: Inspector?,
) {
    private var actions: SingleRequestMap<Any, DeferredAction<*>>? = null

    // For scheduling start/terminate operations in post-evaluation phase
    private var checkToStartActionList: MutableList<DeferredAction<*>>? = null
    private var checkToRemoveActionList: MutableList<DeferredAction<*>>? = null

    /**
     * Validation mode checks that during re-evaluation there were
     * no changes such as new actions declared or existing actions
     * removed.
     */
    private var validationFrame: ActionValidationFrame? = null


    /**
     * Called by FormulaManagerImpl before evaluation that will be run as
     * part of validation.
     */
    fun prepareValidationRun() {
        validationFrame = ActionValidationFrame(
            formulaType = manager.formulaType,
            previousNewActions = checkToStartActionList.orEmpty().toList(),
            previousRemovedActions = checkToRemoveActionList.orEmpty().toList()
        )
    }

    /**
     * Find existing action by key or initialize new one.
     * Schedules new actions for starting.
     * Tracks keys for validation mode.
     *
     * IMPORTANT: Unlike listeners/children, actions do NOT support indexed collision handling.
     * If a key collision occurs, it's treated as an error.
     */
    fun <Event> findOrInitAction(
        key: Any,
        action: Action<Event>,
        listener: Listener<Event>
    ): DeferredAction<Event> {
        // Lazy init actions map
        val actionsMap = actions ?: run {
            val map = mutableMapOf<Any, SingleRequestHolder<DeferredAction<*>>>()
            actions = map
            map
        }

        val holder = actionsMap.getOrInitHolder(key)
        val isNew = holder.isNew() // Call this before requestOrInitValue
        val value = holder.requestOrInitValue { DeferredAction(key, action, listener) }

        // Schedule for starting if it's a new action
        if (isNew) {
            val list = checkToStartActionList ?: mutableListOf<DeferredAction<*>>().also {
                checkToStartActionList = it
            }
            list.add(value)
        }

        @Suppress("UNCHECKED_CAST")
        return value as DeferredAction<Event>
    }

    /**
     * Prepare for post-evaluation phase.
     * Store action keys and schedule unrequested actions for termination.
     */
    fun prepareForPostEvaluation() {
        computeRemovedActionList()
        runValidationIfNeeded()
    }

    /**
     * Returns true if there was a transition while terminating streams.
     */
    fun terminateOld(evaluationId: Long): Boolean {
        val scheduled = checkToRemoveActionList?.takeIf { it.isNotEmpty() } ?: return false

        val iterator = scheduled.iterator()
        while (iterator.hasNext()) {
            val action = iterator.next()
            iterator.remove()

            finishAction(action)

            if (manager.isTerminated()) {
                return false
            }

            if (!manager.canUpdatesContinue(evaluationId)) {
                return true
            }
        }
        return false
    }

    /**
     * Start new actions that were added in latest evaluation.
     */
    fun startNew(evaluationId: Long): Boolean {
        val scheduled = checkToStartActionList?.takeIf { it.isNotEmpty() } ?: return false

        val iterator = scheduled.iterator()
        while (iterator.hasNext()) {
            if (!manager.scope.isActive) {
                // Cannot start any new actions if coroutine scope is not active anymore.
                return false
            }
            
            val action = iterator.next()
            iterator.remove()

            if (!action.isTerminated()) {
                inspector?.onActionStarted(manager.formulaType, action)
                action.start(manager)
            }

            if (manager.isTerminated()) {
                return false
            }

            if (!manager.canUpdatesContinue(evaluationId)) {
                return true
            }
        }

        return false
    }

    /**
     * Terminate all running actions.
     */
    fun terminate() {
        actions?.forEachValue { action ->
            finishAction(action)
        }
        actions?.clear()
    }

    private fun runValidationIfNeeded() {
        // We run validation if validation frame was set for this run
        validationFrame?.validate(
            newStartList = checkToStartActionList.orEmpty(),
            newRemoveList = checkToRemoveActionList.orEmpty()
        )
        validationFrame = null
    }

    /**
     * Called after evaluation to compute which actions were not requested in
     * last evaluation and should be removed.
     */
    private fun computeRemovedActionList() {
        actions?.clearUnrequested { action ->
            val list = checkToRemoveActionList ?: mutableListOf<DeferredAction<*>>().also {
                checkToRemoveActionList = it
            }
            list.add(action)
        }
    }

    private fun finishAction(action: DeferredAction<*>) {
        inspector?.onActionFinished(manager.formulaType, action)
        action.tearDown(manager)
    }
}

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
    private var running: SingleRequestMap<Any, DeferredAction<*>>? = null

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
            manager = manager,
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
        val actionsMap = running ?: run {
            val map = mutableMapOf<Any, SingleRequestHolder<DeferredAction<*>>>()
            running = map
            map
        }

        // Check if this is a new action (key doesn't exist yet)
        val isNew = !actionsMap.containsKey(key)

        val holder = actionsMap.findOrInit(key) {
            DeferredAction(key, action, listener)
        }

        if (holder.requested) {
            // Key collision - this is an error
            val formulaType = manager.formulaType
            val message = "$formulaType - duplicate action key in same evaluation: $key. " +
                    "This likely means the same action is being registered multiple times. " +
                    "Ensure each action has a unique key or is only registered once per evaluation."
            throw IllegalStateException(message)
        }

        // Mark as requested for this evaluation cycle
        holder.requested = true

        // Schedule for starting if it's a new action
        if (isNew) {
            val list = checkToStartActionList ?: mutableListOf<DeferredAction<*>>().also {
                checkToStartActionList = it
            }
            list.add(holder.value)
        }

        @Suppress("UNCHECKED_CAST")
        return holder.value as DeferredAction<Event>
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

            inspector?.onActionStarted(manager.formulaType, action)

            action.start(manager)

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
        running?.forEachValue { action ->
            finishAction(action)
        }
        running?.clear()
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
        running?.clearUnrequested { action ->
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

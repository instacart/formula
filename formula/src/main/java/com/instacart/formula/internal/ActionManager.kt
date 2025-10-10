package com.instacart.formula.internal

import com.instacart.formula.Action
import com.instacart.formula.DeferredAction
import com.instacart.formula.Listener
import com.instacart.formula.plugin.Inspector
import kotlinx.coroutines.isActive
import java.util.LinkedList

/**
 * Handles [DeferredAction] changes.
 */
internal class ActionManager(
    private val manager: FormulaManagerImpl<*, *, *>,
    private val inspector: Inspector?,
) {
    // Validation error tracking
    private var isValidationMode: Boolean = false
    private var validationErrors: LinkedList<ActionValidationError>? = null

    // SingleRequestMap for actions (lazy initialization)
    private var actions: SingleRequestMap<Any, DeferredAction<*>>? = null

    // For scheduling start/terminate operations in post-evaluation phase
    private var checkToStartActionList: MutableList<DeferredAction<*>>? = null
    private var checkToRemoveActionList: MutableList<DeferredAction<*>>? = null

    /**
     * Enable or disable validation mode.
     * Called by FormulaManagerImpl before each evaluation.
     */
    fun setValidationMode(enabled: Boolean) {
        isValidationMode = enabled
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
            if (isValidationMode) {
                val errors = validationErrors ?: LinkedList<ActionValidationError>().also {
                    validationErrors = it
                }
                errors.add(ActionValidationError.NewAction(key))
            }

            val list = checkToStartActionList ?: mutableListOf<DeferredAction<*>>().also {
                checkToStartActionList = it
            }
            list.add(holder.value)
        }

        @Suppress("UNCHECKED_CAST")
        return holder.value as DeferredAction<Event>
    }

    fun runValidation() {
        if (isValidationMode) {
            // Check if any actions would be removed
            actions?.forEach { (_, holder) ->
                if (!holder.requested) {
                    val errors = validationErrors ?: LinkedList<ActionValidationError>().also {
                        validationErrors = it
                    }
                    errors.add(ActionValidationError.RemovedAction(holder.value.key))
                }
            }

            val errors = validationErrors
            if (errors != null && errors.isNotEmpty()) {
                val newActionKeys = errors.mapNotNull { (it as? ActionValidationError.NewAction)?.newActionKey }
                val oldActionKeys = errors.mapNotNull { (it as? ActionValidationError.RemovedAction)?.removedActionKey }
                errors.clear()

                val formulaType = manager.formulaType
                throw ValidationException(
                    "$formulaType - actions changed during validation - new: $newActionKeys, removed: $oldActionKeys"
                )
            }
        }
    }

    /**
     * Prepare for post-evaluation phase.
     * Store action keys and schedule unrequested actions for termination.
     *
     * Called by FormulaManagerImpl after evaluation completes.
     */
    fun prepareForPostEvaluation() {
        // Mark unrequested actions for termination
        // In validation mode, this list should be empty (verified above)
        // Skip if no actions were ever registered
        actions?.clearUnrequested { action ->
            val list = checkToRemoveActionList ?: mutableListOf<DeferredAction<*>>().also {
                checkToRemoveActionList = it
            }
            list.add(action)
        }
    }

    /**
     * Returns true if there was a transition while terminating streams.
     * Simplified - no diffing needed, just process scheduled terminations.
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
     * Simplified - no diffing needed, just process scheduled starts.
     */
    fun startNew(evaluationId: Long): Boolean {
        val scheduled = checkToStartActionList?.takeIf { it.isNotEmpty() } ?: return false

        val iterator = scheduled.iterator()
        while (iterator.hasNext()) {
            val action = iterator.next()
            iterator.remove()

            if (!manager.scope.isActive) {
                return false
            }

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
     * Called when formula is being terminated.
     */
    fun terminate() {
        actions?.let { actionsMap ->
            actionsMap.forEachValue { action ->
                finishAction(action)
            }
            actionsMap.clear()
        }
    }

    private fun finishAction(action: DeferredAction<*>) {
        inspector?.onActionFinished(manager.formulaType, action)
        action.tearDown(manager)
    }
}

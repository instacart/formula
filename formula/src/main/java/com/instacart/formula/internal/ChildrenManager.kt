package com.instacart.formula.internal

import com.instacart.formula.IFormula
import com.instacart.formula.Inspector

/**
 * Keeps track of child formula managers.
 */
internal class ChildrenManager(
    private val delegate: ManagerDelegate,
    private val inspector: Inspector?,
) {
    private var children: SingleRequestMap<Any, FormulaManager<*, *>>? = null
    private var pendingRemoval: MutableList<FormulaManager<*, *>>? = null

    private val actionsToStart = PendingFormulaManagerList { manager, id ->
        manager.startNewUpdates(id)
    }
    private val actionsToRemove = PendingFormulaManagerList { manager, id ->
        manager.terminateOldUpdates(id)
    }

    private val hasDetachedChildren = PendingFormulaManagerList { manager, id ->
        manager.terminateDetachedChildren(id)
    }

    fun updateTransitionId(transitionId: TransitionId) {
        children?.forEachValue { it.updateTransitionId(transitionId) }
    }

    fun evaluationFinished() {
        children?.clearUnrequested {
            pendingRemoval = pendingRemoval ?: mutableListOf()
            it.markAsTerminated()
            pendingRemoval?.add(it)
        }

        actionsToStart.evaluationFinished()
        actionsToRemove.evaluationFinished()
        hasDetachedChildren.evaluationFinished()
    }

    fun terminateDetachedChildren(transitionId: TransitionId): Boolean {
        val local = pendingRemoval
        pendingRemoval = null
        local?.forEach { it.performTerminationSideEffects() }
        if (transitionId.hasTransitioned()) {
            return true
        }

        return hasDetachedChildren.iterate(children, transitionId)
    }

    fun terminateOldUpdates(transitionId: TransitionId): Boolean {
        return actionsToRemove.iterate(children, transitionId)
    }

    fun startNewUpdates(transitionId: TransitionId): Boolean {
        return actionsToStart.iterate(children, transitionId)
    }

    fun markAsTerminated() {
        children?.forEachValue { it.markAsTerminated() }
    }

    fun performTerminationSideEffects() {
        children?.forEachValue { it.performTerminationSideEffects() }
    }

    fun <ChildInput, ChildOutput> findOrInitChild(
        key: Any,
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput,
    ): FormulaManager<ChildInput, ChildOutput> {
        @Suppress("UNCHECKED_CAST")
        val children = children ?: run {
            val initialized: SingleRequestMap<Any, FormulaManager<*, *>> = LinkedHashMap()
            this.children = initialized
            initialized
        }

        return children
            .findOrInit(key) {
                val implementation = formula.implementation()
                FormulaManagerImpl(delegate, implementation, input, inspector = inspector)
            }
            .requestAccess {
                "There already is a child with same key: $key. Override [Formula.key] function."
            } as FormulaManager<ChildInput, ChildOutput>
    }
}
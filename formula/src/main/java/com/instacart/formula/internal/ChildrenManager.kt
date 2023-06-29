package com.instacart.formula.internal

import com.instacart.formula.IFormula
import com.instacart.formula.Inspector

/**
 * Keeps track of child formula managers.
 */
internal class ChildrenManager(
    private val delegate: FormulaManagerImpl<*, *, *>,
    private val inspector: Inspector?,
) {
    private var children: SingleRequestMap<Any, FormulaManager<*, *>>? = null
    private var pendingRemoval: MutableList<FormulaManager<*, *>>? = null

    private val actionsToStart = PendingFormulaManagerList(delegate) { manager ->
        manager.startNewUpdates()
    }
    private val actionsToRemove = PendingFormulaManagerList(delegate) { manager ->
        manager.terminateOldUpdates()
    }

    private val hasDetachedChildren = PendingFormulaManagerList(delegate) { manager ->
        manager.terminateDetachedChildren()
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

    fun terminateDetachedChildren(transitionID: Long): Boolean {
        val local = pendingRemoval
        pendingRemoval = null
        local?.forEach { it.performTerminationSideEffects() }
        if (delegate.hasTransitioned(transitionID)) {
            return true
        }

        return hasDetachedChildren.iterate(children, transitionID)
    }

    fun terminateOldUpdates(transitionID: Long): Boolean {
        return actionsToRemove.iterate(children, transitionID)
    }

    fun startNewUpdates(transitionID: Long): Boolean {
        return actionsToStart.iterate(children, transitionID)
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
package com.instacart.formula.internal

import com.instacart.formula.IFormula

/**
 * Keeps track of child formula managers.
 */
internal class ChildrenManager(
    private val childTransitionListener: TransitionListener,
) {
    private var children: SingleRequestMap<Any, FormulaManager<*, *>>? = null
    private var pendingRemoval: MutableList<FormulaManager<*, *>>? = null

    fun updateTransitionId(transitionId: TransitionId) {
        children?.forEachValue { it.updateTransitionId(transitionId) }
    }

    fun evaluationFinished() {
        children?.clearUnrequested {
            pendingRemoval = pendingRemoval ?: mutableListOf()
            it.markAsTerminated()
            pendingRemoval?.add(it)
        }
    }

    fun terminateDetachedChildren(transitionId: TransitionId): Boolean {
        val local = pendingRemoval
        pendingRemoval = null
        local?.forEach { it.performTerminationSideEffects() }
        if (transitionId.hasTransitioned()) {
            return true
        }

        return children?.any { it.value.value.terminateDetachedChildren(transitionId) } ?: false
    }

    fun terminateOldUpdates(transitionId: TransitionId): Boolean {
        children?.forEachValue {
            if (it.terminateOldUpdates(transitionId)) {
                return true
            }
        }
        return false
    }

    fun startNewUpdates(transitionId: TransitionId): Boolean {
        children?.forEachValue {
            if (it.startNewUpdates(transitionId)) {
                return true
            }
        }
        return false
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
                FormulaManagerImpl(implementation, input, childTransitionListener)
            }
            .requestAccess {
                throw IllegalStateException("There already is a child with same key: $key. Override [Formula.key] function.")
            } as FormulaManager<ChildInput, ChildOutput>
    }
}
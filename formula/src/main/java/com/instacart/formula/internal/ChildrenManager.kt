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

    fun evaluationFinished() {
        children?.clearUnrequested {
            pendingRemoval = pendingRemoval ?: mutableListOf()
            it.markAsTerminated()
            pendingRemoval?.add(it)
        }
    }

    fun terminateChildren(transitionID: Long): Boolean {
        val local = pendingRemoval
        pendingRemoval = null
        local?.forEach { it.performTerminationSideEffects() }
        return !delegate.canUpdatesContinue(transitionID)
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
                FormulaManagerImpl(delegate, implementation, input, loggingType = formula::class, inspector = inspector)
            }
            .requestAccess {
                "There already is a child with same key: $key. Override [Formula.key] function."
            } as FormulaManager<ChildInput, ChildOutput>
    }
}
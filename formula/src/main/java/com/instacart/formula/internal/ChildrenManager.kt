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

    /**
     * After evaluation, we iterate over detached child formulas, mark them as terminated
     * and add them to [pendingRemoval] list. The work to clean them up will be performed
     * in post evaluation, which will call [terminateChildren] function.
     */
    fun prepareForPostEvaluation() {
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

        if (delegate.isTerminated()) {
            return false
        }

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
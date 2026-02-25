package com.instacart.formula.internal

import com.instacart.formula.IFormula
import com.instacart.formula.plugin.ChildAlreadyUsedException
import com.instacart.formula.plugin.FormulaError

/**
 * Keeps track of child formula managers.
 */
internal class ChildrenManager(
    private val manager: FormulaManagerImpl<*, *, *>,
    private val indexer: Indexer,
) {
    private val children: SingleRequestMap<Any, FormulaManager<*, *>> = LinkedHashMap()
    private var pendingRemoval: MutableList<FormulaManager<*, *>>? = null

    private var duplicateKeyLogs: MutableSet<Any>? = null

    /**
     * After evaluation, we iterate over detached child formulas, mark them as terminated
     * and add them to [pendingRemoval] list. The work to clean them up will be performed
     * in post evaluation, which will call [terminateChildren] function.
     */
    fun prepareForPostEvaluation() {
        children.clearUnrequested(this::prepareForTermination)
    }

    fun terminateChildren(evaluationId: Long): Boolean {
        val local = pendingRemoval
        pendingRemoval = null
        local?.forEach { it.performTerminationSideEffects() }

        if (manager.isTerminated()) {
            return false
        }

        return !manager.canUpdatesContinue(evaluationId)
    }

    fun markAsTerminated() {
        children.forEachValue { it.markAsTerminated() }
    }

    fun performTerminationSideEffects(executeTransitionQueue: Boolean) {
        children.forEachValue { it.performTerminationSideEffects(executeTransitionQueue) }
    }

    fun <ChildInput, ChildOutput> findOrInitChild(
        key: Any,
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput,
    ): FormulaManager<ChildInput, ChildOutput> {
        val childManagerEntry = getOrInitChildManager(key, formula, input)
        return if (childManagerEntry.requested) {
            val logs = duplicateKeyLogs ?: run {
                val newSet = mutableSetOf<Any>()
                duplicateKeyLogs = newSet
                newSet
            }

            /**
             * Since child key was already requested, we use a fallback mechanism that
             * uses index increment to handle collisions. This is better than crashing, but
             * can lead to subtle bugs when content shifts. We notify the global listener
             * so it can be logged and fixed by defining explicit key.
             */
            if (logs.add(key)) {
                val error = FormulaError.ChildKeyAlreadyUsed(
                    error = ChildAlreadyUsedException(
                        parentType = manager.formulaType,
                        childType = formula.type(),
                        key = key
                    )
                )
                manager.onError(error)
            }

            val index = indexer.nextIndex(key)
            val indexedKey = IndexedKey(key, index)
            findOrInitChild(indexedKey, formula, input)
        } else {
            childManagerEntry.requested = true
            childManagerEntry.value
        }
    }

    private fun prepareForTermination(it: FormulaManager<*, *>) {
        val list = pendingRemoval ?: mutableListOf()
        pendingRemoval = list
        it.markAsTerminated()
        list.add(it)
    }

    private fun <ChildInput, ChildOutput> getOrInitChildManager(
        key: Any,
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput,
    ): SingleRequestHolder<FormulaManager<ChildInput, ChildOutput>> {
        val childFormulaHolder = children.findOrInit(key) {
            val implementation = formula.implementation
            FormulaManagerImpl(
                delegate = manager,
                formula = implementation,
                formulaType = formula.type(),
                initialInput = input,
            )
        }
        @Suppress("UNCHECKED_CAST")
        return childFormulaHolder as SingleRequestHolder<FormulaManager<ChildInput, ChildOutput>>
    }

}
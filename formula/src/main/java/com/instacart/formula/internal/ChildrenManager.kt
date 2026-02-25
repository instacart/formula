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
        val holder = getOrInitHolder<ChildInput, ChildOutput>(key)
        return if (holder.requested) {
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

            val indexedKey = indexer.nextIndexedKey(key)
            findOrInitChild(indexedKey, formula, input)
        } else {
            holder.requestOrInitValue {
                val implementation = formula.implementation
                FormulaManagerImpl(
                    delegate = manager,
                    formula = implementation,
                    formulaType = formula.type(),
                    initialInput = input,
                )
            }
        }
    }

    private fun prepareForTermination(it: FormulaManager<*, *>) {
        val list = pendingRemoval ?: mutableListOf()
        pendingRemoval = list
        it.markAsTerminated()
        list.add(it)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <ChildInput, ChildOutput> getOrInitHolder(
        key: Any
    ): SingleRequestHolder<FormulaManager<ChildInput, ChildOutput>> {
        return children.getOrInitHolder(key) as SingleRequestHolder<FormulaManager<ChildInput, ChildOutput>>
    }
}
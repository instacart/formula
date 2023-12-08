package com.instacart.formula.internal

import com.instacart.formula.FormulaPlugins
import com.instacart.formula.IFormula
import com.instacart.formula.Inspector
import java.lang.IllegalStateException

/**
 * Keeps track of child formula managers.
 */
internal class ChildrenManager(
    private val delegate: FormulaManagerImpl<*, *, *>,
    private val inspector: Inspector?,
) {
    private var children: SingleRequestMap<Any, FormulaManager<*, *>>? = null
    private var indexes: MutableMap<Any, Int>? = null
    private var pendingRemoval: MutableList<FormulaManager<*, *>>? = null

    private var duplicateKeyLogs: MutableSet<Any>? = null

    /**
     * After evaluation, we iterate over detached child formulas, mark them as terminated
     * and add them to [pendingRemoval] list. The work to clean them up will be performed
     * in post evaluation, which will call [terminateChildren] function.
     */
    fun prepareForPostEvaluation() {
        indexes?.clear()

        children?.clearUnrequested {
            pendingRemoval = pendingRemoval ?: mutableListOf()
            it.markAsTerminated()
            pendingRemoval?.add(it)
        }
    }

    fun terminateChildren(evaluationId: Long): Boolean {
        val local = pendingRemoval
        pendingRemoval = null
        local?.forEach { it.performTerminationSideEffects() }

        if (delegate.isTerminated()) {
            return false
        }

        return !delegate.canUpdatesContinue(evaluationId)
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
        val childHolder = childFormulaHolder(key, formula, input)
        return if (childHolder.requested) {
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
                FormulaPlugins.onDuplicateChildKey(
                    parentFormulaType = delegate.loggingType.java,
                    childFormulaType = formula.type().java,
                    key = key,
                )
            }

            if (key is IndexedKey) {
                // This should never happen, but added as safety
                throw IllegalStateException("Key already indexed (and still duplicate).")
            }

            val index = nextIndex(key)
            val indexedKey = IndexedKey(key, index)
            findOrInitChild(indexedKey, formula, input)
        } else {
            childHolder.requestAccess {
                "There already is a child with same key: $key. Override [Formula.key] function."
            }
        }
    }

    private fun <ChildInput, ChildOutput>  childFormulaHolder(
        key: Any,
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput,
    ): SingleRequestHolder<FormulaManager<ChildInput, ChildOutput>> {
        @Suppress("UNCHECKED_CAST")
        val children = children ?: run {
            val initialized: SingleRequestMap<Any, FormulaManager<*, *>> = LinkedHashMap()
            this.children = initialized
            initialized
        }

        val childFormulaHolder = children.findOrInit(key) {
            val implementation = formula.implementation()
            FormulaManagerImpl(
                delegate,
                implementation,
                input,
                loggingType = formula::class,
                inspector = inspector
            )
        }
        @Suppress("UNCHECKED_CAST")
        return childFormulaHolder as SingleRequestHolder<FormulaManager<ChildInput, ChildOutput>>
    }

    /**
     * Function which returns next index for a given key. It will
     * mutate the [indexes] map.
     */
    private fun nextIndex(key: Any): Int {
        val indexes = indexes ?: run {
            val initialized = mutableMapOf<Any, Int>()
            this.indexes = initialized
            initialized
        }

        val index = indexes.getOrElse(key) { 0 } + 1
        indexes[key] = index
        return index
    }
}
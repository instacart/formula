package com.instacart.formula.lifecycle

import com.instacart.formula.internal.FormulaManagerImpl
import com.instacart.formula.internal.Indexer
import com.instacart.formula.internal.SingleRequestHolder
import com.instacart.formula.internal.SingleRequestMap
import com.instacart.formula.internal.clearUnrequested
import com.instacart.formula.internal.forEachValue

internal class LifecycleCache(
    private val manager: FormulaManagerImpl<*, *, *>,
) : LifecycleScheduler, DuplicateKeyLog {

    private val indexer = Indexer()

    private var entryMap: SingleRequestMap<Any, LifecycleComponent>? = null
    private var terminateEffects: MutableList<() -> Unit>? = null
    private var duplicateKeyLogs: MutableSet<Any>? = null

    override fun scheduleTerminateEffect(effect: () -> Unit) {
        val list = terminateEffects ?: mutableListOf<() -> Unit>().also { terminateEffects = it }
        list.add(effect)
    }

    override fun addLog(key: Any): Boolean {
        val logs = duplicateKeyLogs ?: mutableSetOf<Any>().also { duplicateKeyLogs = it }
        return logs.add(key)
    }

    inline fun <T : LifecycleComponent> findOrInit(
        key: Any,
        useIndex: Boolean,
        factory: () -> T,
    ): T {
        val holder = getOrInitEntryHolder<T>(key, useIndex)
        return holder.requestOrInitValue(factory)
    }

    fun postEvaluationCleanup() {
        indexer.clear()
        entryMap?.clearUnrequested(this::detachComponent)
    }

    fun terminateDetached(evaluationId: Long): Boolean {
        val effects = terminateEffects?.takeIf { it.isNotEmpty() } ?: return false
        terminateEffects = null

        for (effect in effects) {
            effect()
        }

        if (manager.isTerminated()) {
            return false
        }

        return !manager.canUpdatesContinue(evaluationId)
    }

    fun markAsTerminated() {
        entryMap?.forEachValue { it.markAsTerminated() }
    }

    fun performTermination() {
        entryMap?.forEachValue { it.performTermination() }
    }

    private fun detachComponent(component: LifecycleComponent) {
        component.onDetached(this)
    }

    @PublishedApi
    internal fun <T : LifecycleComponent> getOrInitEntryHolder(key: Any, useIndex: Boolean): SingleRequestHolder<T> {
        val holder = findEntry<T>(key)
        return if (holder == null) {
            initNewHolder(key)
        } else if (!holder.requested) {
            holder
        } else if (useIndex) {
            holder.value.onDuplicateKey(this, key)
            val indexedKey = indexer.nextIndexedKey(key)
            getOrInitEntryHolder(indexedKey, true)
        } else {
            holder.value.onDuplicateKey(this, key)
            holder
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : LifecycleComponent> initNewHolder(key: Any): SingleRequestHolder<T> {
        val holder = SingleRequestHolder<LifecycleComponent>(key)
        getOrInitMap().put(key, holder)
        return holder as SingleRequestHolder<T>
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : LifecycleComponent> findEntry(key: Any): SingleRequestHolder<T>? {
        return entryMap?.get(key) as? SingleRequestHolder<T>
    }

    private fun getOrInitMap(): SingleRequestMap<Any, LifecycleComponent> {
        return entryMap ?: run {
            val initialized: SingleRequestMap<Any, LifecycleComponent> = mutableMapOf()
            this.entryMap = initialized
            initialized
        }
    }
}

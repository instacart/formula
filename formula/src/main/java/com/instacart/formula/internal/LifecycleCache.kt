package com.instacart.formula.internal

internal class LifecycleCache(private val indexer: Indexer) : LifecycleScheduler, DuplicateKeyLog {

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
        entryMap?.clearUnrequested { component ->
            component.onDetached(this)
        }
    }

    fun processTerminateEffects() {
        val effects = terminateEffects ?: return
        terminateEffects = null
        effects.forEach { it() }
    }

    fun forEachValue(callback: (LifecycleComponent) -> Unit) {
        entryMap?.forEachValue(callback)
    }

    private fun <T : LifecycleComponent> getOrInitEntryHolder(key: Any, useIndex: Boolean): SingleRequestHolder<T> {
        val holder = findEntry<T>(key)
        return if (holder == null) {
            initNewHolder(key)
        } else if (holder.requested && useIndex) {
            holder.value.onDuplicateKey(this, key)
            val indexedKey = indexer.nextIndexedKey(key)
            getOrInitEntryHolder(indexedKey, true)
        } else {
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

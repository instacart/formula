package com.instacart.formula.lifecycle

import com.instacart.formula.runtime.FormulaManagerImpl
import com.instacart.formula.utils.Indexer
import kotlinx.coroutines.isActive

internal class LifecycleCacheImpl(
    private val manager: FormulaManagerImpl<*, *, *>,
) : LifecycleCache, LifecycleScheduler, DuplicateKeyLog {

    private val indexer = Indexer()

    private val validationManager: LifecycleValidationManager? =
        if (manager.isValidationConfigured) LifecycleValidationManager(manager.formulaType)
        else null

    private var entryMap: SingleRequestMap<Any, LifecycleComponent>? = null
    private var startEffects: MutableList<() -> Unit>? = null
    private var terminateEffects: MutableList<() -> Unit>? = null
    private var duplicateKeyLogs: MutableSet<Any>? = null

    // ==========================================================================
    // LifecycleScheduler
    // ==========================================================================

    override fun scheduleStartEffect(effect: () -> Unit) {
        val list = startEffects ?: mutableListOf<() -> Unit>().also { startEffects = it }
        list.add(effect)
    }

    override fun scheduleTerminateEffect(effect: () -> Unit) {
        val list = terminateEffects ?: mutableListOf<() -> Unit>().also { terminateEffects = it }
        list.add(effect)
    }

    // ==========================================================================
    // DuplicateKeyLog
    // ==========================================================================

    override fun addLog(key: Any): Boolean {
        val logs = duplicateKeyLogs ?: mutableSetOf<Any>().also { duplicateKeyLogs = it }
        return logs.add(key)
    }

    // ==========================================================================
    // LifecycleCache
    // ==========================================================================

    override fun <T : LifecycleComponent> findOrInit(
        key: Any,
        useIndex: Boolean,
        factory: () -> T,
    ): T {
        val holder = getOrInitEntryHolder<T>(key, useIndex)
        val isNew = holder.isNew() // Call this before requestOrInitValue
        return holder.requestOrInitValue(factory).apply {
            if (isNew) {
                validationManager?.trackNewKey(key)
                holder.value.onAttached(this@LifecycleCacheImpl)
            }
        }
    }

    // ==========================================================================
    // Internal
    // ==========================================================================

    fun prepareValidationRun() {
        validationManager?.prepareValidationRun()
    }

    fun postEvaluationCleanup() {
        indexer.clear()
        entryMap?.clearUnrequested(this::detachComponent)
        validationManager?.validate()
    }

    /**
     * Returns true if there was a transition while executing termination effects.
     */
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

    /**
     * Performs scheduled start effects. Returns true if there was a transition
     * while executing effect.
     */
    fun startAttached(evaluationId: Long): Boolean {
        val scheduled = startEffects?.takeIf { it.isNotEmpty() } ?: return false

        val iterator = scheduled.iterator()
        while (iterator.hasNext()) {
            if (!manager.scope.isActive) {
                return false
            }

            val effect = iterator.next()
            iterator.remove()

            effect()

            if (manager.isTerminated()) {
                return false
            }

            if (!manager.canUpdatesContinue(evaluationId)) {
                return true
            }
        }

        return false
    }

    fun markAsTerminated() {
        entryMap?.forEachValue { it.markAsTerminated() }
    }

    fun performTermination() {
        entryMap?.forEachValue { it.performTermination() }
    }

    private fun detachComponent(key: Any, component: LifecycleComponent) {
        validationManager?.trackRemovedKey(key)
        component.onDetached(this)
    }

    private fun <T : LifecycleComponent> getOrInitEntryHolder(key: Any, useIndex: Boolean): SingleRequestHolder<T> {
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

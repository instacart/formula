package com.instacart.formula.internal

import com.instacart.formula.Effect
import com.instacart.formula.batch.BatchManager
import com.instacart.formula.plugin.Dispatcher
import com.instacart.formula.plugin.FormulaError
import com.instacart.formula.plugin.Inspector
import kotlinx.coroutines.CoroutineScope

/**
 * Used by [FormulaManagerImpl] to delegate and request certain actions when it
 * cannot handle them internally.
 */
internal interface ManagerDelegate {
    val batchManager: BatchManager
    val defaultDispatcher: Dispatcher
    val inspector: Inspector?
    val scope: CoroutineScope
    val queue: SynchronizedUpdateQueue
    val onError: (FormulaError) -> Unit

    /**
     * Returns true if the delegate (parent) has been terminated.
     */
    fun isTerminated(): Boolean

    /**
     * When a transition happens, we notify the parent if we need to re-evaluate or
     * we have global transition effects that need to be executed or both.
     */
    fun onPostTransition(effects: List<Effect>, evaluate: Boolean)
}
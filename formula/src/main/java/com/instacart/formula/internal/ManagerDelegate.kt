package com.instacart.formula.internal

/**
 * Used by [FormulaManagerImpl] to delegate and request certain actions when it
 * cannot handle them internally.
 */
internal interface ManagerDelegate {

    /**
     * When [FormulaManagerImpl] is not currently running and needs to be evaluated again, it
     * will request the parent delegate to trigger a new evaluation run.
     */
    fun requestEvaluation()

    /**
     * When [FormulaManagerImpl] is not terminated nor running, it will pass the transition
     * to the parent delegate to handle.
     */
    fun onPendingTransition(transition: DeferredTransition<*, *, *>)
}
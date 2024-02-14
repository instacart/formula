package com.instacart.formula.internal

import com.instacart.formula.Effect

/**
 * Used by [FormulaManagerImpl] to delegate and request certain actions when it
 * cannot handle them internally.
 */
internal interface ManagerDelegate {

    /**
     * When a transition happens, we notify the parent if we need to re-evaluate or
     * we have global transition effects that need to be executed or both.
     */
    fun onPostTransition(effects: List<Effect>, evaluate: Boolean)
}
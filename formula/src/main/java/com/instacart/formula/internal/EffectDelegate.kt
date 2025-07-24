package com.instacart.formula.internal

import com.instacart.formula.plugin.FormulaError

/**
 * Interface required to execute effects.
 */
interface EffectDelegate {
    val formulaType: Class<*>
    val onError: (FormulaError) -> Unit
}

internal fun EffectDelegate.onEffectError(throwable: Throwable) {
    val error = FormulaError.EffectError(formulaType, throwable)
    onError(error)
}
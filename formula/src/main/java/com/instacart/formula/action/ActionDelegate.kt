package com.instacart.formula.action

import com.instacart.formula.plugin.FormulaError
import com.instacart.formula.plugin.Inspector
import kotlinx.coroutines.CoroutineScope

/**
 * Interface used by [ActionComponent] to start and stop [com.instacart.formula.Action].
 */
internal interface ActionDelegate {
    val scope: CoroutineScope
    val formulaType: Class<*>
    val inspector: Inspector?
    val onError: (FormulaError) -> Unit
}

internal inline fun ActionDelegate.runSafe(action: () -> Unit) {
    try {
        action()
    } catch (throwable: Throwable) {
        onActionError(throwable)
    }
}

internal fun ActionDelegate.onActionError(throwable: Throwable) {
    val error = FormulaError.ActionError(formulaType, throwable)
    onError(error)
}
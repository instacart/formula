package com.instacart.formula.validation

import com.instacart.formula.DeferredAction
import com.instacart.formula.internal.FormulaManagerImpl
import com.instacart.formula.internal.ValidationException
import kotlin.collections.filter
import kotlin.collections.map

/**
 * Validation mode checks that during re-evaluation there were
 * no changes such as new actions declared or existing actions
 * removed.
 *
 * @param previousNewActions New actions defined during last evaluation
 * @param previousRemovedActions Removed actions during last evaluation
 */
internal class ActionValidationFrame(
    private val manager: FormulaManagerImpl<*, *, *>,
    private val previousNewActions: List<DeferredAction<*>>,
    private val previousRemovedActions: List<DeferredAction<*>>,
) {

    fun validate(
        newStartList: List<DeferredAction<*>>,
        newRemoveList: List<DeferredAction<*>>,
    ) {
        val newActionKeys = newStartList
            .filter { !previousNewActions.contains(it) }
            .map { it.key }

        val removedActionKeys = newRemoveList
            .filter { !previousRemovedActions.contains(it) }
            .map { it.key }

        if (newActionKeys.isNotEmpty() || removedActionKeys.isNotEmpty()) {
            val formulaType = manager.formulaType
            throw ValidationException(
                "$formulaType - actions changed during validation - new: $newActionKeys, removed: $removedActionKeys"
            )
        }
    }

}
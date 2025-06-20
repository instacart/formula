package com.instacart.formula.plugin

sealed class FormulaError {

    data class ActionError(
        val formula: Class<*>,
        val error: Throwable,
    ): FormulaError()
}
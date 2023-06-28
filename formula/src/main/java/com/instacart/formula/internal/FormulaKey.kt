package com.instacart.formula.internal

internal data class FormulaKey(
    val scopeKey: Any?,
    val type: Class<*>,
    val key: Any?
)

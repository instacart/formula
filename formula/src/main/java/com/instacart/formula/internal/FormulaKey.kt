package com.instacart.formula.internal

internal data class FormulaKey(
    private val scopeKey: Any?,
    private val type: Class<*>,
    private val key: Any?
)

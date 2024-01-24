package com.instacart.formula.internal

internal data class ActionKey(
    val id: Long,
    private val delegateKey: Any?,
)
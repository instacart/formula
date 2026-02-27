package com.instacart.formula.action

internal data class ActionKey(
    val id: Long,
    private val delegateKey: Any?,
)
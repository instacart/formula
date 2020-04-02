package com.instacart.formula.integration

data class FlowEnvironment<Key>(
    val onScreenError: (Key, Throwable) -> Unit = { _, it -> throw it }
)

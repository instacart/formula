package com.instacart.formula.integration

data class FlowEnvironment<in Key>(
    val onScreenError: (Key, Throwable) -> Unit = { _, it -> throw it }
)

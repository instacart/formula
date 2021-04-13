package com.instacart.formula.counter

data class CounterRenderModel(
    val count: String,
    val onDecrement: () -> Unit,
    val onIncrement: () -> Unit
)

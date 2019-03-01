package com.instacart.formula.counter

class CounterRenderModel(
    val count: String,
    val onDecrement: () -> Unit,
    val onIncrement: () -> Unit
)

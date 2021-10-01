package com.instacart.formula.counter

import com.instacart.formula.Listener

data class CounterRenderModel(
    val count: String,
    val onDecrement: Listener<Unit>,
    val onIncrement: Listener<Unit>,
)

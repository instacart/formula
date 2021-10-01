package com.instacart.formula.samples.composition.item

import com.instacart.formula.Listener

data class ItemRenderModel(
    val name: String,
    val displayQuantity: String,
    val isDecrementEnabled: Boolean,
    val onDecrement: Listener<Unit>,
    val onIncrement: Listener<Unit>,
)
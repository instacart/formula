package com.instacart.formula.samples.composition.item

data class ItemRenderModel(
    val name: String,
    val displayQuantity: String,
    val isDecrementEnabled: Boolean,
    val onDecrement: () -> Unit,
    val onIncrement: () -> Unit
)
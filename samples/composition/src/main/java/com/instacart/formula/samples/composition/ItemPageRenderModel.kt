package com.instacart.formula.samples.composition

import com.instacart.formula.samples.composition.item.ItemRenderModel

data class ItemPageRenderModel(
    val title: String,
    val items: List<ItemRenderModel>
)
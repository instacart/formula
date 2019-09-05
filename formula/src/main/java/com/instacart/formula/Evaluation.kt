package com.instacart.formula

/**
 * Represents the result of [Formula.evaluate].
 */
data class Evaluation<RenderModel>(
    val renderModel: RenderModel,
    val updates: List<Update<*>> = emptyList()
)

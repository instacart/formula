package com.instacart.formula

/**
 * Represents the result of [ProcessorFormula.evaluate].
 */
data class Evaluation<RenderModel>(
    val renderModel: RenderModel,
    val updates: List<StreamConnection<*, *>> = emptyList()
)

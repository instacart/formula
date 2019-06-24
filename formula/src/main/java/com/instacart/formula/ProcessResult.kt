package com.instacart.formula

/**
 * Represents the result of [ProcessorFormula.process].
 */
data class ProcessResult<RenderModel>(
    val renderModel: RenderModel,
    val streams: List<StreamConnection<*, *>> = emptyList()
)

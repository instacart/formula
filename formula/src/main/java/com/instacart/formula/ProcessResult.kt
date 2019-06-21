package com.instacart.formula

data class ProcessResult<RenderModel>(
    val renderModel: RenderModel,
    val workers: List<Worker<*, *>> = emptyList()
)

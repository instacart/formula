package com.instacart.formula

/**
 * Render view defines how to render a model of type [RenderModel].
 */
interface RenderView<in RenderModel> {
    val render: Render<RenderModel>
}

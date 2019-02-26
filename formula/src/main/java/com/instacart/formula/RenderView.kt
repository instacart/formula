package com.instacart.formula

/**
 * A [RenderView] is a provider of a [Renderer] that can render specified [RenderModel].
 */
interface RenderView<in RenderModel> {
    val renderer: Renderer<RenderModel>
}

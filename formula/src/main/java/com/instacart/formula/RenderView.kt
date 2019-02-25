package com.instacart.formula

/**
 * Mvi view provides a renderer that will render the [State]
 */
interface RenderView<in State> {
    val renderer: Renderer<State>
}

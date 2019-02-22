package com.instacart.client.mvi

/**
 * Mvi view provides a renderer that will render the [State]
 */
interface ICMviView<in State> {
    val renderer: ICRenderer<State>
}
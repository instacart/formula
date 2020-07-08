package com.instacart.formula

/**
 * The result of [evaluate][Formula.evaluate] function.
 *
 * @param Output Usually a data class returned by formula that contains data and callbacks.
 * When it is used to render UI, we call it a render model (Ex: ItemRenderModel).
 *
 * @param updates A list of asynchronous events that formula wants to listen and respond to.
 */
data class Evaluation<Output>(
    val output: Output,
    val updates: List<Update<*>> = emptyList()
)

package com.instacart.formula

/**
 * The result of [evaluate][Formula.evaluate] function.
 *
 * @param Output Usually a data class returned by formula that contains data and event
 * listeners. When it is used to render UI, we call it a render model (Ex: ItemRenderModel).
 *
 * @param actions A set of deferred actions registered during formula evaluation.
 * See [Action] and [ActionBuilder].
 */
data class Evaluation<out Output>(
    val output: Output,
    val actions: Set<DeferredAction<*>> = emptySet(),
)

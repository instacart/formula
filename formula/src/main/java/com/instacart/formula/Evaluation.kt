package com.instacart.formula

/**
 * Legacy wrapper for formula output. [Formula.evaluate] now returns [Output] directly; this type
 * remains available for source compatibility.
 *
 * @param Output Usually a data class returned by formula that contains data and event
 * listeners. When it is used to render UI, we call it a render model (Ex: ItemRenderModel).
 *
 * @param actions A list of deferred actions which Formula runtime will execute.
 * See [Action] and [FormulaContext.actions].
 */
data class Evaluation<out Output>(
    val output: Output,
    val actions: Set<DeferredAction<*>> = emptySet(),
)

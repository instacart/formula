package com.instacart.formula

/**
 * The result of [evaluate][Formula.evaluate] function.
 *
 * @param Output Usually a data class returned by formula that contains data and event
 * listeners. When it is used to render UI, we call it a render model (Ex: ItemRenderModel).
 *
 * @param actions A list of deferred actions which Formula runtime will execute.
 * See [Action] and [FormulaContext.actions].
 */
data class Evaluation<out Output>(
    val output: Output,
    val actions: List<DeferredAction<*>> = emptyList(),
) {
    companion object {
        @Deprecated(
            message = "Replace `updates` with `actions`.",
            replaceWith = ReplaceWith(
                "Evaluation(output = output, actions = updates)",
                "com.instacart.formula.Evaluation"
            )
        )
        operator fun <Output> invoke(output: Output, updates: List<DeferredAction<*>>): Evaluation<Output> {
            return Evaluation(
                output = output,
                actions = updates
            )
        }
    }
}

package com.instacart.formula

/**
 * Version of [IFormula] which has no internal state.
 *
 * @param Input A data class provided by the parent that contains data and event listeners. Input
 * change will trigger [Formula.evaluate] to be called and new [Output] will be created.
 * Use [Unit] type when there is no input.
 *
 * @param Output A data class returned by this formula that contains data and event
 * listeners. When it is used to render UI, we call it a render model (Ex: ItemRenderModel).
 */
abstract class StatelessFormula<Input, Output> : IFormula<Input, Output> {

    // Implements the common API used by the runtime.
    private val implementation = object : Formula<Input, Unit, Output>() {
        override fun initialState(input: Input) = Unit

        override fun Snapshot<Input, Unit>.evaluate(): Evaluation<Output> {
            return this@StatelessFormula.run { evaluate() }
        }
    }

    /**
     * The primary purpose of evaluate is to create an [output][Evaluation.output]. A
     * [snapshot][Snapshot] which contains current [Input] and [Formula context][FormulaContext] is
     * passed to evaluation and should be used to build the [Output]. Within this method, we can use
     * Formula context to [compose][FormulaContext.child] child formulas, create event listeners
     * using [FormulaContext.onEvent], and [respond][FormulaContext.updates] to arbitrary
     * asynchronous events.
     *
     * Whenever [input][Input] or child output changes, a new [Snapshot] will be created and
     * [evaluate] will be called again.
     *
     * ### Warning
     * Do not access mutable state or emit side-effects as part of [evaluate] function.
     * All side-effects should happen as part of event listeners or [updates][Evaluation.updates].
     */
     abstract fun Snapshot<Input, Unit>.evaluate(): Evaluation<Output>

    /**
     * A unique identifier used to distinguish formulas of the same type.
     *
     * ```
     * override fun key(input: ItemInput) = input.itemId
     * ```
     */
    override fun key(input: Input): Any? = null

    override fun implementation(): Formula<Input, *, Output> = implementation
}

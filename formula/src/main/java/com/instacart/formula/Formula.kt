package com.instacart.formula

/**
 * Represents a composable, stateful, reactive program that takes an [input][Input] and
 * produces an [output][Output].
 *
 * @param Input A data class provided by the parent that contains data and event listeners. Input
 * change will trigger [Formula.onInputChanged] and [Formula.evaluate] to be called and
 * new [Output] will be created. Use [Unit] type when there is no input.
 *
 * @param State Usually a data class that represents internal state used within this formula.
 *
 * @param Output A data class returned by this formula that contains data and event
 * listeners. When it is used to render UI, we call it a render model (Ex: ItemRenderModel).
 */
abstract class Formula<in Input, State, out Output> : IFormula<Input, Output> {
    final override val implementation: Formula<Input, *, Output>
        get() = this

    /**
     * Creates the initial [state][State] to be used in [evaluation][Formula.evaluate]. This
     * method is called when formula first starts running or when the [key] changes.
     */
    abstract fun initialState(input: Input): State

    /**
     * This method is called if [Input] changes while [Formula] is already running. It
     * is called before invoking [evaluate]. You can use this method to change the [State]
     * in response to [Input] change.
     */
    open fun onInputChanged(
        oldInput: Input,
        input: Input,
        state: State
    ): State = state

    /**
     * The primary purpose of evaluate is to create an [output][Evaluation.output]. A
     * [snapshot][Snapshot] which contains current [Input], current [State] and
     * [formula context][FormulaContext] is passed to evaluation and should be used to build
     * the [Output]. Within this method, we can use Formula context to
     * [compose][FormulaContext.child] child formulas, create event listeners using
     * [FormulaContext.onEvent], and [respond][FormulaContext.actions] to arbitrary asynchronous
     * events.
     *
     * Whenever [input][Input], [internal state][State] or child output changes, a new [Snapshot]
     * will be created and [evaluate] will be called again.
     *
     * ### Warning
     * Do not access mutable state or emit side-effects as part of [evaluate] function.
     * All side-effects should happen as part of event listeners or [actions][Evaluation.actions].
     */
    abstract fun Snapshot<Input, State>.evaluate(): Evaluation<Output>

    /**
     * A unique identifier used to distinguish formulas of the same type. This can also
     * be used to [restart][Formula.initialState] formula when some input property changes.
     * ```
     * override fun key(input: ItemInput) = input.itemId
     * ```
     */
    override fun key(input: Input): Any? = null
}

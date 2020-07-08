package com.instacart.formula

/**
 * Represents a composable, stateful, reactive program that takes an [input][Input] and
 * produces an [output][Output].
 *
 * @param Input A data class provided by the parent that contains data and callbacks. Input change
 * will trigger [Formula.onInputChanged] and [Formula.evaluate] to be called and new [Output] will
 * be created. Use [Unit] type when there is no input.
 *
 * @param State Usually a data class that represents internal state used within this formula.
 *
 * @param Output A data class returned by this formula that contains data and callbacks. When it is
 * used to render UI, we call it a render model (Ex: ItemRenderModel).
 */
interface Formula<Input, State, Output> : IFormula<Input, Output> {

    /**
     * Instantiate initial [State].
     */
    fun initialState(input: Input): State

    /**
     * This method is called if [Input] changes while [Formula] is already running. It
     * is called before invoking [evaluate]. You can use this method to change the [State]
     * in response to [Input] change.
     */
    fun onInputChanged(
        oldInput: Input,
        input: Input,
        state: State
    ): State = state

    /**
     * This method is called any time there is:
     * 1. A [State] change
     * 2. A parent [Formula] calls [FormulaContext.child] with a new [Input].
     * 3. A child [Formula] has an internal state change.
     *
     * As part of this method:
     * 1. Use [FormulaContext.child] to define children formulas.
     * 2. Use [FormulaContext.updates] to define side effects and asynchronous event listeners.
     * 3. Return an [Evaluation] with the current [Output].
     *
     * Do not emit side-effects internally before returning [Evaluation]. All side-effects should happen as part of
     * event callbacks or [Evaluation.updates].
     */
    fun evaluate(
        input: Input,
        state: State,
        context: FormulaContext<State>
    ): Evaluation<Output>

    override fun implementation(): Formula<Input, *, Output> {
        return this
    }
}

package com.instacart.formula

/**
 * Formula interface defines render model management.
 *
 * @param Input - defines data that the parent/host can pass to this formula.
 * @param State - internal state that is used for this formula.
 * @param Output - a type of message that can be passed to the parent/host.
 * @param RenderModel - a type that is used to render this formula UI.
 */
interface Formula<Input, State, Output, RenderModel> {

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
     * 3. A child [Formula] has an internal state change or produces an output.
     *
     * As part of this method:
     * 1. Use [FormulaContext.child] to define children formulas.
     * 2. Use [FormulaContext.updates] to define side effects and asynchronous event listeners.
     * 3. Return an [Evaluation] with the current [RenderModel].
     *
     * Do not emit side-effects internally before returning [Evaluation]. All side-effects should happen as part of
     * event callbacks or [Evaluation.updates].
     */
    fun evaluate(
        input: Input,
        state: State,
        context: FormulaContext<State, Output>
    ): Evaluation<RenderModel>
}

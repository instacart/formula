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
     * This method is called any time [State] changes. Use this method to
     * 1. Create the [RenderModel]
     * 2. Define what [Stream]s should run.
     * 3. Define children Formulas.
     */
    fun evaluate(
        input: Input,
        state: State,
        context: FormulaContext<State, Output>
    ): Evaluation<RenderModel>
}

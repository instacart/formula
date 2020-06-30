package com.instacart.formula

import io.reactivex.rxjava3.core.Observable

/**
 * Formula interface defines render model management.
 *
 * @param Input Defines data that the parent/host can pass to this formula.
 * @param State Internal state that is used within this formula.
 * @param RenderModel A type that is used to render UI.
 */
interface Formula<Input, State, RenderModel> {

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
     * 3. Return an [Evaluation] with the current [RenderModel].
     *
     * Do not emit side-effects internally before returning [Evaluation]. All side-effects should happen as part of
     * event callbacks or [Evaluation.updates].
     */
    fun evaluate(
        input: Input,
        state: State,
        context: FormulaContext<State>
    ): Evaluation<RenderModel>

    fun start(): Observable<RenderModel> {
        return start(input = Unit as Input)
    }

    fun start(
        input: Input
    ): Observable<RenderModel> {
        return start(input = Observable.just(input))
    }

    fun start(
        input: Observable<Input>
    ): Observable<RenderModel> {
        return FormulaRuntime.start(input = input, formula = this)
    }
}

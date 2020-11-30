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
interface Formula<Input, State : Any, Output> : IFormula<Input, Output> {

    /**
     * Creates the initial [state][State] to be used in [evaluation][Formula.evaluate]. This
     * method is called when formula first starts running or when the [key] changes.
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
     * The primary purpose of evaluate is to create an [output][Evaluation.output]. Within
     * this method, we can also [compose][FormulaContext.child] child formulas, handle
     * callbacks [with data][FormulaContext.eventCallback] or [without data][FormulaContext.callback],
     * and [respond][FormulaContext.updates] to arbitrary asynchronous events.
     *
     * Evaluate will be called whenever [input][Input], [internal state][State] or child output changes.
     *
     * ### Warning
     * Do not access mutable state or emit side-effects as part of [evaluate] function.
     * All side-effects should happen as part of event callbacks or [updates][Evaluation.updates].
     */
    fun evaluate(
        input: Input,
        state: State,
        context: FormulaContext<State>
    ): Evaluation<Output>

    /**
     * A unique identifier used to distinguish formulas of the same type. This can also
     * be used to [restart][Formula.initialState] formula when some input property changes.
     * ```
     * override fun key(input: ItemInput) = input.itemId
     * ```
     */
    override fun key(input: Input): Any? = null

    override fun implementation(): Formula<Input, *, Output> {
        return this
    }

    companion object {
        // Used to attach extension functions.
    }
}

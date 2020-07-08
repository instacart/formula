package com.instacart.formula

/**
 * Version of [IFormula] which has no internal state.
 *
 * @param Input A data class provided by the parent that contains data and callbacks. Input change
 * will trigger [Formula.evaluate] to be called and new [Output] will be created.
 * Use [Unit] type when there is no input.
 *
 * @param Output A data class returned by this formula that contains data and callbacks. When it is
 * used to render UI, we call it a render model (Ex: ItemRenderModel).
 */
abstract class StatelessFormula<Input, Output> : IFormula<Input, Output> {

    // Implements the common API used by the runtime.
    private val implementation = object : Formula<Input, Unit, Output> {
        override fun initialState(input: Input) = Unit

        override fun evaluate(
            input: Input,
            state: Unit,
            context: FormulaContext<Unit>
        ): Evaluation<Output> {
            return evaluate(input, context)
        }
    }

    /**
     * The primary purpose of evaluate is to create an [output][Evaluation.renderModel]. Within
     * this method, we can also [compose][FormulaContext.child] child formulas, handle
     * callbacks [with data][FormulaContext.eventCallback] or [without data][FormulaContext.callback],
     * and [respond][FormulaContext.updates] to arbitrary asynchronous events.
     *
     * Evaluate will be called whenever [input][Input] or child output changes.
     *
     * ### Warning
     * Do not access mutable state or emit side-effects as part of [evaluate] function.
     * All side-effects should happen as part of event callbacks or [updates][Evaluation.updates].
     */
     abstract fun evaluate(
        input: Input,
        context: FormulaContext<Unit>
    ): Evaluation<Output>

    override fun implementation(): Formula<Input, *, Output> = implementation
}

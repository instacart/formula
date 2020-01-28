package com.instacart.formula

/**
 * Version of [Formula] which requires no state
 */
abstract class StatelessFormula<Input, RenderModel> : Formula<Input, Unit, RenderModel> {

    final override fun initialState(input: Input) = Unit

    final override fun evaluate(
        input: Input,
        state: Unit,
        context: FormulaContext<Unit>
    ): Evaluation<RenderModel> {
        return evaluate(input, context)
    }

    /**
     * Same as [evaluate] but just with the needed input and context
     */
     abstract fun evaluate(
        input: Input,
        context: FormulaContext<Unit>
    ): Evaluation<RenderModel>
}

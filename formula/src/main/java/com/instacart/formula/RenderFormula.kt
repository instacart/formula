package com.instacart.formula

/**
 * Version of [Formula] which requires no state or input
 */
abstract class RenderFormula<RenderModel> : Formula<Unit, Unit, RenderModel> {

    final override fun initialState(input: Unit) = Unit

    final override fun evaluate(
        input: Unit,
        state: Unit,
        context: FormulaContext<Unit>
    ): Evaluation<RenderModel> {
        return evaluate(context)
    }

    /**
     * Same as [evaluate] but just with the needed context
     */
    abstract fun evaluate(
        context: FormulaContext<Unit>
    ): Evaluation<RenderModel>
}

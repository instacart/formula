package com.instacart.formula

/**
 * Version of [Formula] which requires no input
 */
abstract class InputlessFormula<State, RenderModel> : Formula<Unit, State, RenderModel> {

    final override fun evaluate(
        input: Unit,
        state: State,
        context: FormulaContext<State>
    ): Evaluation<RenderModel> {
        return evaluate(state, context)
    }

    /**
     * Same as [evaluate] but just with the needed state and context
     */
     abstract fun evaluate(
        state: State,
        context: FormulaContext<State>
    ): Evaluation<RenderModel>
}

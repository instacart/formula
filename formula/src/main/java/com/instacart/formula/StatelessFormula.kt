package com.instacart.formula

abstract class StatelessFormula<Input, Output, RenderModel> : Formula<Input, Unit, Output, RenderModel> {

    final override fun initialState(input: Input) = Unit

    final override fun evaluate(
        input: Input,
        state: Unit,
        context: FormulaContext<Unit, Output>
    ): Evaluation<RenderModel> {
        return evaluate(input, context)
    }

     abstract fun evaluate(
        input: Input,
        context: FormulaContext<Unit, Output>
    ): Evaluation<RenderModel>
}
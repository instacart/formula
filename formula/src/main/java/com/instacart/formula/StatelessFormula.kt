package com.instacart.formula

abstract class StatelessFormula<Input, RenderModel> : Formula<Input, Unit, RenderModel> {

    final override fun initialState(input: Input) = Unit

    final override fun evaluate(
        input: Input,
        state: Unit,
        context: FormulaContext<Unit>
    ): Evaluation<RenderModel> {
        return evaluate(input, context)
    }

     abstract fun evaluate(
        input: Input,
        context: FormulaContext<Unit>
    ): Evaluation<RenderModel>
}

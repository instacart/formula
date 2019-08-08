package com.instacart.formula

class OnlyUpdateFormula<Input>(
    private val build: FormulaContext.UpdateBuilder<Unit>.(Input) -> Unit
) : StatelessFormula<Input, Unit>() {

    override fun evaluate(input: Input, context: FormulaContext<Unit>): Evaluation<Unit> {
        return Evaluation(
            renderModel = Unit,
            updates = context.updates {
                build(this, input)
            }
        )
    }
}

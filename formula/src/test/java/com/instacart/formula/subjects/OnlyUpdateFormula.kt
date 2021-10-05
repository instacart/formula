package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.FormulaContext
import com.instacart.formula.StatelessFormula
import com.instacart.formula.StreamBuilder

class OnlyUpdateFormula<Input>(
    private val build: StreamBuilder<Unit>.(Input) -> Unit
) : StatelessFormula<Input, Unit>() {

    override fun evaluate(input: Input, context: FormulaContext<Unit>): Evaluation<Unit> {
        return Evaluation(
            output = Unit,
            updates = context.updates {
                build(this, input)
            }
        )
    }
}

package com.instacart.formula.subjects

import com.instacart.formula.ActionBuilder
import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula

class OnlyUpdateFormula<Input>(
    private val build: ActionBuilder<*, Unit>.(Input) -> Unit
) : StatelessFormula<Input, Unit>() {

    override fun Snapshot<Input, Unit>.evaluate(): Evaluation<Unit> {
        return Evaluation(
            output = Unit,
            actions = context.actions {
                build(this, input)
            }
        )
    }
}

package com.instacart.formula.subjects

import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula

class OnlyUpdateFormula<Input>(
    private val build: Snapshot<*, Unit>.(Input) -> Unit
) : StatelessFormula<Input, Unit>() {

    override fun Snapshot<Input, Unit>.evaluate(): Unit {
        buildAction()
        return Unit
    }

    private fun Snapshot<Input, Unit>.buildAction() {
        build(this, input)
    }
}

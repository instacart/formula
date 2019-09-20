package com.instacart.formula

import com.instacart.formula.utils.TestUtils

object OnlyUpdateFormula {

    fun <Input> create(
        build: FormulaContext.UpdateBuilder<Unit>.(Input) -> Unit
    ) = TestUtils.stateless { input: Input, context ->
        Evaluation(
            renderModel = Unit,
            updates = context.updates {
                build(input)
            }
        )
    }
}

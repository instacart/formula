package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.Listener

class NestedKeyFormula : Formula<Unit, Unit, NestedKeyFormula.Output>() {

    class Output(val callback: Listener<Unit>)

    override fun initialState(input: Unit) = Unit

    override fun evaluate(
        input: Unit,
        state: Unit,
        context: FormulaContext<Unit>
    ): Evaluation<Output> {

        val callback = context.key("first level") {
            context.key("second level") {
                context.onEvent<Unit> {
                    none()
                }
            }
        }

        return Evaluation(
            output = Output(callback)
        )
    }
}

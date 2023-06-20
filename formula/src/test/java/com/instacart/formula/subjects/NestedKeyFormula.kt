package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot

class NestedKeyFormula : Formula<Unit, Unit, NestedKeyFormula.Output>() {

    data class Output(val callback: Listener<Unit>)

    override fun initialState(input: Unit) = Unit

    override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Output> {

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

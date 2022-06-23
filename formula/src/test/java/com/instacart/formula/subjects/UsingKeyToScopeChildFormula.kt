package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot

class UsingKeyToScopeChildFormula : Formula<Unit, Unit, UsingKeyToScopeChildFormula.Output>() {

    data class Output(val children: List<String>)

    private val childFormula = UseInputFormula<String>()

    override fun initialState(input: Unit) = Unit

    override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Output> {

        val child1 = context.key("first") {
            context.child(childFormula, "value 1")
        }

        val child2 = context.key("second") {
            context.child(childFormula, "value 2")
        }

        return Evaluation(
            output = Output(listOf(child1, child2))
        )
    }
}

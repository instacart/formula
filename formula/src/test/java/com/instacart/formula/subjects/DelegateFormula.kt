package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot

class DelegateFormula<Type>(
    private val initialValue: Type
) : Formula<Unit, Type, DelegateFormula.Output<Type>>() {

    data class Output<Type>(
        val childValue: Type,
        val changeChildInput: (Type) -> Unit
    )

    private val childFormula = UseInputFormula<Type>()

    override fun initialState(input: Unit): Type = initialValue

    override fun Snapshot<Unit, Type>.evaluate(): Evaluation<Output<Type>> {
        return Evaluation(
            output = Output(
                childValue = context.child(childFormula, state),
                changeChildInput = context.onEvent<Type> { name -> transition(name) }
            )
        )
    }
}
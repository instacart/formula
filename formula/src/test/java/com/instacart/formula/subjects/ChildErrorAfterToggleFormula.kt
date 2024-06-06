package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.subjects.ChildErrorAfterToggleFormula.Output
import com.instacart.formula.subjects.ChildErrorAfterToggleFormula.State

class ChildErrorAfterToggleFormula: Formula<HasChildrenFormula.ChildParamsInput<()-> Unit>, State, Output>()  {
    data class Output(
        val errorToggle: () -> Unit,
        val listener: () -> Unit
    )

    data class State(val throwError: Boolean = false)

    override fun initialState(input: HasChildrenFormula.ChildParamsInput<()-> Unit>) = State()

    override fun Snapshot<HasChildrenFormula.ChildParamsInput<()-> Unit>, State>.evaluate(): Evaluation<Output> {
        if (state.throwError) throw RuntimeException()
        return Evaluation(
            output = Output(
                errorToggle = context.callback() {
                    transition(state.copy(throwError = !state.throwError))
                },
                listener = context.callback {
                    transition { input.value() }
                }
            ),
        )
    }
}
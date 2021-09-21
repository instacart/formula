package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.IFormula

class OptionalChildFormula<ChildInput, ChildOutput>(
    private val child: IFormula<ChildInput, ChildOutput>,
    private val childInput: FormulaContext<State>.(State) -> ChildInput
): Formula<Unit, OptionalChildFormula.State, OptionalChildFormula.Output<ChildOutput>> {
    companion object {
        operator fun <ChildOutput> invoke(child: IFormula<Unit, ChildOutput>) = run {
            OptionalChildFormula(child) { Unit }
        }
    }

    data class State(
        val showChild: Boolean = true
    )

    class Output<ChildOutput>(
        val child: ChildOutput?,
        val toggleChild: () -> Unit
    )

    override fun initialState(input: Unit) = State()

    override fun evaluate(
        input: Unit,
        state: State,
        context: FormulaContext<State>
    ): Evaluation<Output<ChildOutput>> {
        val childOutput = if (state.showChild) {
            context.child(child, childInput(context, state))
        } else {
            null
        }

        return Evaluation(
            output = Output(
                child = childOutput,
                toggleChild = context.onEvent {
                    transition(state.copy(showChild = !state.showChild))
                }
            )
        )
    }
}

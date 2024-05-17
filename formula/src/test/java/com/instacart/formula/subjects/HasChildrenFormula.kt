package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.IFormula
import com.instacart.formula.Snapshot
import com.instacart.formula.subjects.HasChildrenFormula.State
import com.instacart.formula.subjects.HasChildrenFormula.Output

class HasChildrenFormula<ChildInput, ChildOutput>(
    private val childCount: Int,
    private val child: IFormula<ChildInput, ChildOutput>,
    private val createChildInput: FormulaContext<*, State>.(Int) -> ChildInput
) : Formula<Unit, State, Output<ChildOutput>>() {

    data class State(
        val errors: List<Throwable> = emptyList(),
    )

    data class Output<ChildOutput>(
        val errors: List<Throwable>,
        val childOutputs: List<ChildOutput>,
    )

    override fun initialState(input: Unit): State = State()

    override fun Snapshot<Unit, State>.evaluate(): Evaluation<Output<ChildOutput>> {
        val childOutputs = (0 until childCount).mapNotNull{ index ->
            context.key(index) {
                context.child(
                    formula = child,
                    input = createChildInput(context, index),
                    onError = context.onEvent { error ->
                        transition(state.copy(errors = state.errors + error))
                    }
                )
            }
        }
        return Evaluation(
            output = Output(
                errors = state.errors,
                childOutputs = childOutputs,
            )
        )
    }
}

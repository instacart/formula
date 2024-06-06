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
    private val child: IFormula<ChildParamsInput<ChildInput>, ChildOutput>,
    private val createChildInput: FormulaContext<*, State>.(ChildParams) -> ChildParamsInput<ChildInput>,
) : Formula<Int, State, Output<ChildOutput>>() {

    companion object {
        operator fun <ChildOutput> invoke(
            childCount:Int,
            child: IFormula<ChildParamsInput<Unit>, ChildOutput>
        ): HasChildrenFormula<Unit, ChildOutput> {
            return HasChildrenFormula(childCount, child) {
                ChildParamsInput(
                    index = it.index,
                    run = it.run,
                    value = Unit,
                )
            }
        }
    }
    data class ChildParams(
        val index: Int,
        val run: Int,
    )

    data class ChildParamsInput<Input>(
        val index: Int,
        val run: Int,
        val value: Input,
    )

    data class State(
        val errors: List<Throwable> = emptyList(),
    )

    data class Output<ChildOutput>(
        val run: Int,
        val errors: List<Throwable>,
        val childOutputs: List<ChildOutput>,
    )

    override fun initialState(input: Int): State = State()

    override fun Snapshot<Int, State>.evaluate(): Evaluation<Output<ChildOutput>> {
        val childOutputs = (0 until childCount).mapNotNull{ index ->
            context.key(index) {
                val childParams = ChildParams(index = index, run = input)
                val childInput = createChildInput(context, childParams)
                context.child(
                    formula = child,
                    input = childInput,
                    onError = context.onEvent { error ->
                        transition(state.copy(errors = state.errors + error))
                    }
                )
            }
        }
        return Evaluation(
            output = Output(
                run = input,
                errors = state.errors,
                childOutputs = childOutputs,
            )
        )
    }
}

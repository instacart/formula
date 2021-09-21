package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.IFormula

class HasChildFormula<ChildInput, ChildOutput>(
    private val child: IFormula<ChildInput, ChildOutput>,
    private val createChildInput: FormulaContext<Int>.(Int) -> ChildInput
) : Formula<Unit, Int, HasChildFormula.Output<ChildOutput>> {
    companion object {
        operator fun <ChildOutput> invoke(
            child: IFormula<Unit, ChildOutput>
        ): HasChildFormula<Unit, ChildOutput> {
            return HasChildFormula(child) { Unit }
        }
    }


    data class Output<ChildOutput>(
        val state: Int,
        val child: ChildOutput
    )

    override fun initialState(input: Unit): Int = 0

    override fun evaluate(
        input: Unit,
        state: Int,
        context: FormulaContext<Int>
    ): Evaluation<Output<ChildOutput>> {
        return Evaluation(
            output = Output(
                state = state,
                child = context.child(child, createChildInput(context, state))
            )
        )
    }
}

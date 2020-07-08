package com.instacart.formula

class SideEffectFormula(
    private val onSideEffect: () -> Unit
) : Formula<Unit, Int, SideEffectFormula.Output> {

    class Output(
        val triggerSideEffect: () -> Unit
    )

    override fun initialState(input: Unit): Int = 0

    override fun evaluate(input: Unit, state: Int, context: FormulaContext<Int>): Evaluation<Output> {
        return Evaluation(
            output = Output(
                triggerSideEffect = context.callback {
                    transition { onSideEffect() }
                }
            )
        )
    }
}

package com.instacart.formula

class EffectWithInputFormula : StatelessFormula<Int, Unit>()  {
    val effects = mutableListOf<Int>()

    override fun evaluate(input: Int, context: FormulaContext<Unit>): Evaluation<Unit> {
        return Evaluation(
            renderModel = Unit,
            updates = context.updates {
                effect("input", input) {
                    effects.add(it)
                }
            }
        )
    }
}

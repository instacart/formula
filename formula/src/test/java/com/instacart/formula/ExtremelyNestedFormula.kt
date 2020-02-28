package com.instacart.formula

object ExtremelyNestedFormula {
    class TestFormula(private val childFormula: Formula<Unit, *, Int>?) : Formula<Unit, Int, Int> {
        override fun initialState(input: Unit): Int = 0

        override fun evaluate(
            input: Unit,
            state: Int,
            context: FormulaContext<Int>
        ): Evaluation<Int> {
            val childValue = if (childFormula != null) {
                context.child(childFormula).input(Unit)
            } else {
                0
            }

            return Evaluation(
                renderModel = state + childValue,
                updates = context.updates {
                    events(Stream.onInit()) {
                        transition(state + 1)
                    }
                }
            )
        }
    }

    fun nested(levels: Int): TestFormula {
        return (1 until levels).fold(TestFormula(null)) { child, value ->
            TestFormula(child)
        }
    }
}

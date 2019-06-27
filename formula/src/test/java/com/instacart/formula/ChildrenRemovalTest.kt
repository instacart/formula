package com.instacart.formula

import org.junit.Test

class ChildrenRemovalTest {

    @Test fun `remove all children`() {
        ParentFormula()
            .state(Unit)
            .test()
            .apply {
                values().last().onClearAll()
            }
            .assertNoErrors()
    }

    class ParentFormula : Formula<Unit, List<Int>, Unit, ParentFormula.RenderModel> {
        class RenderModel(
            val children: List<Unit>,
            val onClearAll: () -> Unit
        )

        override fun initialState(input: Unit) = listOf(1, 2, 3)

        override fun evaluate(
            input: Unit,
            state: List<Int>,
            context: FormulaContext<List<Int>, Unit>
        ): Evaluation<RenderModel> {
            return Evaluation(
                renderModel = RenderModel(
                    children = state.map { id ->
                        context.child(ChildFormula(), Unit, "child-$id") {
                            transition(state)
                        }
                    },
                    onClearAll = {
                        context.transition(emptyList())
                    }
                )
            )
        }
    }

    class ChildFormula : Formula<Unit, Unit, Unit, Unit> {
        override fun initialState(input: Unit) = Unit

        override fun evaluate(input: Unit, state: Unit, context: FormulaContext<Unit, Unit>): Evaluation<Unit> {
            return Evaluation(
                renderModel = Unit
            )
        }
    }
}

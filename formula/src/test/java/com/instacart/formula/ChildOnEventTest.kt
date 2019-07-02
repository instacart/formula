package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ChildOnEventTest {

    @Test
    fun `each child event handler should be scoped to latest state`() {
        ParentFormula().state(Unit).test()
            .apply {
                values().last().child.onIncrement()
                values().last().child.onIncrement()
                values().last().child.onIncrement()
            }
            .apply {
                assertThat(values().last().state).isEqualTo(3)
            }
    }

    class ParentFormula : Formula<Unit, Int, Unit, ParentFormula.RenderModel> {
        private val childFormula = ChildFormula()

        class RenderModel(
            val state: Int,
            val child: ChildFormula.RenderModel
        )

        override fun initialState(input: Unit) = 0

        override fun evaluate(
            input: Unit,
            state: Int,
            context: FormulaContext<Int, Unit>
        ): Evaluation<ParentFormula.RenderModel> {

            return Evaluation(
                renderModel = RenderModel(
                    state = state,
                    child = context.child(childFormula, Unit) {
                        transition(state + 1)
                    }
                )
            )
        }
    }

    class ChildFormula : StatelessFormula<Unit, ChildFormula.Increment, ChildFormula.RenderModel>() {
        class Increment

        class RenderModel(
            val onIncrement: () -> Unit
        )

        override fun evaluate(input: Unit, context: FormulaContext<Unit, Increment>): Evaluation<RenderModel> {
            return Evaluation(
                renderModel = RenderModel(
                    onIncrement = context.callback {
                        output(Increment())
                    }
                )
            )
        }
    }
}

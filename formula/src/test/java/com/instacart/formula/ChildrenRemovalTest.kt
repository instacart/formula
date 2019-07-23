package com.instacart.formula

import com.google.common.truth.Truth.assertThat
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

    @Test fun `child side effects are performed after removal`() {
        var timesLoggedCalled = 0
        ParentFormula(logExit = { timesLoggedCalled += 1 })
            .state(Unit)
            .test()
            .apply {
                values().last().children.first().onExit()
            }
            .apply {
                assertThat(timesLoggedCalled).isEqualTo(1)
            }
    }

    class ParentFormula(
        private val logExit: () -> Unit = {}
    ) : Formula<Unit, List<Int>, Unit, ParentFormula.RenderModel> {
        class RenderModel(
            val children: List<ChildFormula.RenderModel>,
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
                        context.child("child-$id", ChildFormula(logExit), Unit) {
                            transition(state.minus(id))
                        }
                    },
                    onClearAll = context.callback("clear all") {
                        transition(emptyList())
                    }
                )
            )
        }
    }

    class ChildFormula(
        val logExit: () -> Unit
    ) : Formula<Unit, Unit, ChildFormula.Exit, ChildFormula.RenderModel> {
        class RenderModel(
            val onExit: () -> Unit
        )

        class Exit

        override fun initialState(input: Unit) = Unit

        override fun evaluate(input: Unit, state: Unit, context: FormulaContext<Unit, Exit>): Evaluation<RenderModel> {
            return Evaluation(
                renderModel = RenderModel(
                    onExit = context.callback("exit") {
                        transition(state, Exit(), sideEffects = listOf(
                            SideEffect("log exit", logExit)
                        ))
                    }
                )
            )
        }
    }
}

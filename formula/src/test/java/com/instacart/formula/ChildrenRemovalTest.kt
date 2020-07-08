package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ChildrenRemovalTest {

    @Test fun `remove all children`() {
        ParentFormula()
            .start()
            .test()
            .apply {
                values().last().onClearAll()
            }
            .assertNoErrors()
    }

    @Test fun `child side effects are performed after removal`() {
        var timesLoggedCalled = 0
        ParentFormula(logExit = { timesLoggedCalled += 1 })
            .start()
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
    ) : Formula<Unit, List<Int>, ParentFormula.RenderModel> {
        class RenderModel(
            val children: List<ChildFormula.RenderModel>,
            val onClearAll: () -> Unit
        )

        override fun initialState(input: Unit) = listOf(1, 2, 3)

        override fun evaluate(
            input: Unit,
            state: List<Int>,
            context: FormulaContext<List<Int>>
        ): Evaluation<RenderModel> {
            return Evaluation(
                renderModel = RenderModel(
                    children = state.map { id ->
                        val key = "child-$id"
                        val childInput = ChildFormula.Input(
                            exit = context.callback("exit-$id") {
                                transition(state.minus(id))
                            }
                        )
                        context.child(key, ChildFormula(logExit), childInput)
                    },
                    onClearAll = context.callback {
                        transition(emptyList())
                    }
                )
            )
        }
    }

    class ChildFormula(
        val logExit: () -> Unit
    ) : Formula<ChildFormula.Input, Unit, ChildFormula.RenderModel> {

        class Input(
            val exit: () -> Unit
        )

        class RenderModel(
            val onExit: () -> Unit
        )

        override fun initialState(input: Input) = Unit

        override fun evaluate(input: Input, state: Unit, context: FormulaContext<Unit>): Evaluation<RenderModel> {
            return Evaluation(
                renderModel = RenderModel(
                    onExit = context.callback {
                        transition {
                            input.exit()
                            logExit()
                        }
                    }
                )
            )
        }
    }
}

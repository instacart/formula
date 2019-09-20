package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.utils.TestUtils
import org.junit.Test

class ChildrenRemovalTest {

    @Test fun `remove all children`() {
        createParent()
            .start(Unit)
            .test()
            .apply {
                values().last().onClearAll()
            }
            .assertNoErrors()
    }

    @Test fun `child side effects are performed after removal`() {
        var timesLoggedCalled = 0
        createParent(logExit = { timesLoggedCalled += 1 })
            .start(Unit)
            .test()
            .apply {
                values().last().children.first().onExit()
            }
            .apply {
                assertThat(timesLoggedCalled).isEqualTo(1)
            }
    }

    class ParentRenderModel(
        val children: List<ChildRenderModel>,
        val onClearAll: () -> Unit
    )

    fun createParent(
        logExit: () -> Unit = {}
    ) = TestUtils.create(listOf(1, 2, 3)) { state, context ->
        Evaluation(
            renderModel = ParentRenderModel(
                children = state.map { id ->
                    context
                        .child("child-$id", childFormula(logExit))
                        .input(ChildInput(
                            exit = context.callback("exit-$id") {
                                transition(state.minus(id))
                            }
                        ))
                },
                onClearAll = context.callback {
                    transition(emptyList())
                }
            )
        )
    }

    data class ChildInput(val exit: () -> Unit)
    data class ChildRenderModel(val onExit: () -> Unit)

    fun childFormula(logExit: () -> Unit) = TestUtils.stateless { input: ChildInput, context ->
        Evaluation(
            renderModel = ChildRenderModel(
                onExit = context.callback {
                    transition {
                        message(input.exit)
                        message(logExit)
                    }
                }
            )
        )
    }
}

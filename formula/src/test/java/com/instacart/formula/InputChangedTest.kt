package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.utils.TestUtils
import org.junit.Test

class InputChangedTest {

    @Test fun `input changes`() {
        parent().start(Unit).test().apply {
            values().last().onChildNameChanged("first")
            values().last().onChildNameChanged("second")
        }.apply {
            assertThat(values().map { it.childName }).containsExactly("default", "first", "second")
        }
    }

    data class ParentRenderModel(val childName: String, val onChildNameChanged: (String) -> Unit)

    fun parent() = run {
        val childFormula = child()

        TestUtils.create("default") { state, context ->
            Evaluation(
                renderModel = ParentRenderModel(
                    childName = context.child(childFormula).input(state),
                    onChildNameChanged = context.eventCallback { name ->
                        name.noMessages()
                    }
                )
            )
        }
    }

    fun child() = TestUtils.lazyState(
        initialState = { input: String -> input },
        // We override our state with what parent provides.
        onInputChanged = { _, new, _ -> new},
        evaluate = { state, context ->
            Evaluation(renderModel = state)
        }
    )
}

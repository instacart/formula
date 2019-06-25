package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class InputChangedTest {

    @Test fun `input changes`() {
        ParentFormula().state(Unit).test().apply {
            values().last().onChildNameChanged("first")
            values().last().onChildNameChanged("second")
        }.apply {
            assertThat(values().map { it.childName }).containsExactly("default", "first", "second")
        }
    }

    class ParentFormula : ProcessorFormula<Unit, String, Unit, ParentFormula.RenderModel> {
        private val childFormula = ChildFormula()

        class RenderModel(val childName: String, val onChildNameChanged: (String) -> Unit)

        override fun initialState(input: Unit): String = "default"

        override fun process(
            input: Unit,
            state: String,
            context: FormulaContext<String, Unit>
        ): ProcessResult<RenderModel> {
            return ProcessResult(
                renderModel = RenderModel(
                    childName = context.child(childFormula, state) {
                        Transition(state)
                    },
                    onChildNameChanged = {
                        context.transition(it)
                    }
                )
            )
        }
    }

    class ChildFormula : ProcessorFormula<String, String, Unit, String> {
        override fun initialState(input: String): String = input

        override fun onInputChanged(oldInput: String, input: String, state: String): String {
            // We override our state with what parent provides.
            return input
        }

        override fun process(
            input: String,
            state: String,
            context: FormulaContext<String, Unit>
        ): ProcessResult<String> {
            return ProcessResult(
                renderModel = state
            )
        }
    }
}

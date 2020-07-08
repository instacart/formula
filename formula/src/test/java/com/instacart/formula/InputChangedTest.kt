package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class InputChangedTest {

    @Test fun `input changes`() {
        ParentFormula().start().test().apply {
            values().last().onChildNameChanged("first")
            values().last().onChildNameChanged("second")
        }.apply {
            val expected = listOf("default", "first", "second")
            assertThat(values().map { it.childName }).isEqualTo(expected)
        }
    }

    class ParentFormula : Formula<Unit, String, ParentFormula.RenderModel> {
        private val childFormula = ChildFormula()

        class RenderModel(val childName: String, val onChildNameChanged: (String) -> Unit)

        override fun initialState(input: Unit): String = "default"

        override fun evaluate(
            input: Unit,
            state: String,
            context: FormulaContext<String>
        ): Evaluation<RenderModel> {
            return Evaluation(
                renderModel = RenderModel(
                    childName = context.child(childFormula, state),
                    onChildNameChanged = context.eventCallback { name ->
                        name.noEffects()
                    }
                )
            )
        }
    }

    class ChildFormula : Formula<String, String, String> {
        override fun initialState(input: String): String = input

        override fun onInputChanged(oldInput: String, input: String, state: String): String {
            // We override our state with what parent provides.
            return input
        }

        override fun evaluate(
            input: String,
            state: String,
            context: FormulaContext<String>
        ): Evaluation<String> {
            return Evaluation(renderModel = state)
        }
    }
}

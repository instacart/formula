package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import org.junit.Test

class InputChangedTest {

    @Test fun `input changes`() {
        ParentFormula().test(Unit)
            .output { onChildNameChanged("first") }
            .output { onChildNameChanged("second") }
            .apply {
                val expected = listOf("default", "first", "second")
                assertThat(values().map { it.childName }).isEqualTo(expected)
            }
    }

    class ParentFormula : Formula<Unit, String, ParentFormula.Output> {
        private val childFormula = ChildFormula()

        data class Output(val childName: String, val onChildNameChanged: (String) -> Unit)

        override fun initialState(input: Unit): String = "default"

        override fun evaluate(
            input: Unit,
            state: String,
            context: FormulaContext<String>
        ): Evaluation<Output> {
            return Evaluation(
                output = Output(
                    childName = context.child(childFormula, state),
                    onChildNameChanged = context.onEvent { name ->
                        transition(name)
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
            return Evaluation(output = state)
        }
    }
}

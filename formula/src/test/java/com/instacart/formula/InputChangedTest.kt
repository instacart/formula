package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import org.junit.Test

class InputChangedTest {

    @Test
    fun `input changes`() {
        parentFormula().test()
            .output { onChildNameChanged("first") }
            .output { onChildNameChanged("second") }
            .apply {
                val expected = listOf("default", "first", "second")
                assertThat(values().map { it.childName }).isEqualTo(expected)
            }
    }

    data class ParentOutput(
        val childName: String,
        val onChildNameChanged: (String) -> Unit
    )

    private fun parentFormula(): IFormula<Unit, ParentOutput> {
        val childFormula = childFormula()
        return Formula.create("default") { state, context ->
            Evaluation(
                output = ParentOutput(
                    childName = context.child(childFormula, state),
                    onChildNameChanged = context.eventCallback { name ->
                        name.noEffects()
                    }
                )
            )
        }
    }

    private fun childFormula(): IFormula<String, String> {
        return Formula.create(
            initialState = { input: String -> input },
            onInputChanged = { _, new, _ ->
                // We override our state with what parent provides.
                new
            },
            evaluate = { _, state, _ ->
                Evaluation(output = state)
            }
        )
    }
}

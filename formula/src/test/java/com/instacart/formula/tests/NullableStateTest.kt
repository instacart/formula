package com.instacart.formula.tests

import com.google.common.truth.Truth
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.test.test
import org.junit.Test

class NullableStateTest {

    @Test fun `nullable state changes`() {
        TestFormula()
            .test(Unit)
            .output { Truth.assertThat(state).isNull() }
            .output { updateState("new state") }
            .output { Truth.assertThat(state).isEqualTo("new state") }
            .output { updateState(null) }
            .output { Truth.assertThat(state).isNull() }
    }

    class TestFormula : Formula<Unit, String?, TestFormula.Output> {

        data class Output(
            val state: String?,
            val updateState: (String?) -> Unit
        )

        override fun initialState(input: Unit): String? = null

        override fun evaluate(
            input: Unit,
            state: String?,
            context: FormulaContext<String?>
        ): Evaluation<Output> {
            return Evaluation(
                output = Output(
                    state = state,
                    updateState = context.eventCallback {
                        transition(it)
                    }
                )
            )
        }
    }
}
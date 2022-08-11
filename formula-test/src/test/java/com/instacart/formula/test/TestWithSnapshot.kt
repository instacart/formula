package com.instacart.formula.test

import com.google.common.truth.Truth
import com.instacart.formula.FormulaContext
import com.instacart.formula.Listener
import org.junit.Test

class TestWithSnapshot {

    data class Output(
        val state: Int,
        val listener: Listener<Int>,
    )

    private fun createOutput(state: Int, context: FormulaContext<Unit, Int>): Output {
        return Output(
            state = state,
            listener = context.onEvent {
                none()
            }
        )
    }

    @Test fun `withSnapshot provides functional formula context`() {
        val output = withSnapshot(1) { createOutput(state, context) }
        Truth.assertThat(output.state).isEqualTo(1)
    }
}
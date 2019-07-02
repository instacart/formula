package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.junit.Test

class MultipleOutputTest {

    @Test fun `multiple output order should be maintained`() {
        val outputs = mutableListOf<Trigger>()
        TestFormula()
            .state(Input()) {
                outputs.add(it)
            }
            .test()
            .apply {
                assertThat(outputs.map { it.count }).containsExactly(0, 1, 2, 3)
            }
    }

    data class Trigger(val count: Int)
    class Input

    class TestFormula : Formula<Input, Int, Trigger, Unit> {

        override fun initialState(input: Input) = 0

        override fun evaluate(
            input: Input,
            state: Int,
            context: FormulaContext<Int, Trigger>
        ): Evaluation<Unit> {
            return Evaluation(
                renderModel = Unit,
                updates = context.updates {
                    events("changes", Observable.range(0, 4)) {
                        transition(state + 1, Trigger(state))
                    }
                }
            )
        }
    }
}

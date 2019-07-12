package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import io.reactivex.Observable
import org.junit.Test

class MultipleOutputTest {

    @Test fun `multiple output order should be maintained`() {
        TestFormula()
            .test()
            .outputs {
                assertThat(this.map { it.count }).containsExactly(0, 1, 2, 3)
            }
    }

    data class Trigger(val count: Int)

    class TestFormula : Formula<Unit, Int, Trigger, Unit> {

        override fun initialState(input: Unit) = 0

        override fun evaluate(
            input: Unit,
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

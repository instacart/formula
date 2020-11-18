package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.rxjava3.RxStream
import com.instacart.formula.test.test
import io.reactivex.rxjava3.core.Observable
import org.junit.Test

class ManyEmissionStreamTest {
    companion object {
        const val EMISSION_COUNT = 100000
    }

    @Test fun `all increment events go through`() {
        TestFormula()
            .test()
            .apply {
                assertThat(values()).containsExactly(EMISSION_COUNT).inOrder()
            }
    }

    class TestFormula : Formula<Unit, Int, Int> {
        private val stream = RxStream.fromObservable {
            val values = 1..EMISSION_COUNT
            Observable.fromIterable(values)
        }

        override fun initialState(input: Unit): Int = 0

        override fun evaluate(
            input: Unit,
            state: Int,
            context: FormulaContext<Int>
        ): Evaluation<Int> {

            return Evaluation(
                updates = context.updates {
                    events(stream) {
                        transition(state + 1)
                    }
                },
                output = state
            )
        }
    }
}

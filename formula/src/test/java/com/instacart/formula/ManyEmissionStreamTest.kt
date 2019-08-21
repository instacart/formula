package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import io.reactivex.Observable
import org.junit.Test

class ManyEmissionStreamTest {
    companion object {
        const val EMISSION_COUNT = 100000
    }

    @Test fun `all increment events go through`() {
        TestFormula()
            .test()
            .apply {
                assertThat(values()).containsExactly(EMISSION_COUNT)
            }
    }

    class TestFormula : Formula<Unit, Int, Int> {
        override fun initialState(input: Unit): Int = 0

        override fun evaluate(
            input: Unit,
            state: Int,
            context: FormulaContext<Int>
        ): Evaluation<Int> {

            return Evaluation(
                updates = context.updates {
                    events(MyStream()) {
                        transition(state + 1)
                    }
                },
                renderModel = state
            )
        }
    }

    class MyStream : RxStream<Unit, Int> {
        override fun observable(parameter: Unit): Observable<Int> {
            val values = 1..EMISSION_COUNT
            return Observable.fromIterable(values)
        }
    }
}

package com.instacart.formula

import io.reactivex.Observable
import org.junit.Test

class ManyEmissionStreamTest {

    @Test fun `all increment events go through`() {
        TestFormula()
            .state(Unit)
            .test()
            .assertNoErrors()
            .assertValues(100000)
    }

    class TestFormula : Formula<Unit, Int, Unit, Int> {
        override fun initialState(input: Unit): Int = 0

        override fun evaluate(
            input: Unit,
            state: Int,
            context: FormulaContext<Int, Unit>
        ): Evaluation<Int> {

            return Evaluation(
                updates = context.updates {
                    events(MyStream(), Unit) {
                        transition(state + 1)
                    }
                },
                renderModel = state
            )
        }
    }

    class MyStream : RxStream<Unit, Int> {
        override fun observable(input: Unit): Observable<Int> {
            val values = 1..100000
            return Observable.fromIterable(values)
        }
    }
}

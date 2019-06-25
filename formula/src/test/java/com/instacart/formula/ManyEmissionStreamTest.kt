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

    class TestFormula : ProcessorFormula<Unit, Int, Unit, Int> {
        override fun initialState(input: Unit): Int = 0

        override fun process(
            input: Unit,
            state: Int,
            context: FormulaContext<Int, Unit>
        ): ProcessResult<Int> {

            return ProcessResult(
                streams = context.streams {
                    stream(MyStream(), Unit) {
                        Transition(state + 1)
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

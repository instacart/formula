package com.instacart.formula

import io.reactivex.Observable
import org.junit.Test
import java.lang.IllegalStateException

class DuplicateStreamTest {

    @Test fun `duplicate streams throw exception`() {
        val formula = duplicateStream {
            stream(FakeStream()) {
                Transition(Unit)
            }

            stream(FakeStream()) {
                Transition(Unit)
            }
        }

        formula.state(Unit).test().assertError {
            it is IllegalStateException
        }
    }

    private fun duplicateStream(
        build: FormulaContext.StreamBuilder<Unit, Unit>.() -> Unit
    ): ProcessorFormula<Unit, Unit, Unit, Unit> {
        return object : ProcessorFormula<Unit, Unit, Unit, Unit> {
            override fun initialState(input: Unit) = Unit

            override fun process(input: Unit, state: Unit, context: FormulaContext<Unit, Unit>): ProcessResult<Unit> {
                return ProcessResult(
                    renderModel = Unit,
                    streams = context.streams(build)
                )
            }
        }
    }

    class FakeStream : RxStream<Unit, Unit> {
        override fun observable(input: Unit): Observable<Unit> {
            return Observable.empty()
        }
    }
}

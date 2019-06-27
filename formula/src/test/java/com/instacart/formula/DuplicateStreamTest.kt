package com.instacart.formula

import io.reactivex.Observable
import org.junit.Test
import java.lang.IllegalStateException

class DuplicateStreamTest {

    @Test fun `duplicate streams throw exception`() {
        val formula = duplicateStream {
            events(FakeStream()) {
                transition(Unit)
            }

            events(FakeStream()) {
                transition(Unit)
            }
        }

        formula.state(Unit).test().assertError {
            it is IllegalStateException
        }
    }

    private fun duplicateStream(
        build: FormulaContext.UpdateBuilder<Unit, Unit>.() -> Unit
    ): Formula<Unit, Unit, Unit, Unit> {
        return object : Formula<Unit, Unit, Unit, Unit> {
            override fun initialState(input: Unit) = Unit

            override fun evaluate(input: Unit, state: Unit, context: FormulaContext<Unit, Unit>): Evaluation<Unit> {
                return Evaluation(
                    renderModel = Unit,
                    updates = context.updates(build)
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

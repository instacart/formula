package com.instacart.formula

import com.google.common.truth.Truth
import io.reactivex.Observable
import org.junit.Test

class FakeDbSideEffectTest {

    @Test fun `fake saving to db test`() {
        val saveCalls = mutableListOf<String>()
        TestFormula(
            nameChanges = Observable.just("first", "second", "third", "third"),
            saveToDb = {
                saveCalls.add(it)
            }
        )
            .state(Unit)
            .test()
            .assertNoErrors()

        Truth.assertThat(saveCalls).containsExactly("first", "second", "third")
    }

    class TestFormula(
        private val nameChanges: Observable<String>,
        private val saveToDb: (name: String) -> Unit
    ) : Formula<Unit, TestFormula.State, Unit, String> {

        data class State(val name: String, val saveToDb: String? = null)

        override fun initialState(input: Unit): State = State(name = "")

        override fun evaluate(input: Unit, state: State, context: FormulaContext<State, Unit>): Evaluation<String> {
            return Evaluation(
                renderModel = state.name,
                updates = context.updates {
                    events("nameChanges", nameChanges) {
                        transition(state.copy(name = it, saveToDb = it))
                    }

                    if (state.saveToDb != null) {
                        effect("db updates", state.saveToDb, saveToDb)
                    }
                }
            )
        }
    }
}

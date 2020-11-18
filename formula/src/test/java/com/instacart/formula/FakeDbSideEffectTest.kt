package com.instacart.formula

import com.google.common.truth.Truth
import com.instacart.formula.rxjava3.toObservable
import io.reactivex.rxjava3.core.Observable
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
            .toObservable()
            .test()
            .assertNoErrors()

        Truth.assertThat(saveCalls).containsExactly("first", "second", "third", "third").inOrder()
    }

    class TestFormula(
        private val nameChanges: Observable<String>,
        private val saveToDb: (name: String) -> Unit
    ) : Formula<Unit, TestFormula.State, String> {

        data class State(val name: String)

        override fun initialState(input: Unit): State = State(name = "")

        override fun evaluate(input: Unit, state: State, context: FormulaContext<State>): Evaluation<String> {
            return Evaluation(
                output = state.name,
                updates = context.updates {
                    RxStream.fromObservable { nameChanges }.onEvent { newName ->
                        transition(state.copy(name = newName)) {
                            saveToDb(newName)
                        }
                    }
                }
            )
        }
    }
}

package com.instacart.formula

import com.google.common.truth.Truth
import com.instacart.formula.utils.TestUtils
import io.reactivex.Observable
import org.junit.Test

class FakeDbSideEffectTest {

    @Test fun `fake saving to db test`() {
        val saveCalls = mutableListOf<String>()
        formula(
            nameChanges = Observable.just("first", "second", "third", "third"),
            saveToDb = { saveCalls.add(it) }
        )
            .start(Unit)
            .test()
            .assertNoErrors()

        Truth.assertThat(saveCalls).containsExactly("first", "second", "third", "third")
    }

    data class State(val name: String)

    fun formula (
        nameChanges: Observable<String>,
        saveToDb: (name: String) -> Unit
    ) = TestUtils.create(initialState = State(name = "")) { state, context ->
        Evaluation(
            renderModel = state.name,
            updates = context.updates {
                events(nameChanges) { newName ->
                    transition(state.copy(name = newName)) {
                        message(saveToDb, newName)
                    }
                }
            }
        )
    }
}

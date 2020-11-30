package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.rxjava3.RxStream
import com.instacart.formula.test.test
import io.reactivex.rxjava3.core.Observable
import org.junit.Test

class FetchDataExampleTest {

    @Test fun `fake network example`() {
        formula()
            .test()
            .apply {
                values().last().onChangeId("1")
                values().last().onChangeId("2")
            }
            .apply {
                val expected = listOf("", "response: 1", "response: 2")
                assertThat(values().map { it.title }).isEqualTo(expected)
            }
    }

    class DataRepo {
        data class Response(val id: String, val name: String)

        fun fetch(id: String) = RxStream.fromObservable(id) {
            Observable.just(Response(id = id, name = "response: $id"))
        }
    }

    data class State(
        val selectedId: String? = null,
        val response: DataRepo.Response? = null
    )

    class Output(
        val title: String,
        val onChangeId: (String) -> Unit
    )

    private fun formula(): IFormula<Unit, Output> {
        val dataRepo = DataRepo()
        return Formula.create(State()) { state, context ->
            Evaluation(
                output = Output(
                    title = state.response?.name ?: "",
                    onChangeId = context.eventCallback { id ->
                        transition(state.copy(selectedId = id))
                    }
                ),
                updates = context.updates {
                    if (state.selectedId != null) {
                        dataRepo.fetch(state.selectedId).onEvent { response ->
                            transition(state.copy(response = response))
                        }
                    }
                }
            )
        }
    }
}

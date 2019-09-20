package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import com.instacart.formula.utils.TestUtils
import io.reactivex.Observable
import org.junit.Test

class FetchDataExampleTest {

    @Test fun `fake network example`() {
        MyFormula
            .create()
            .test()
            .apply {
                values().last().onChangeId("1")
                values().last().onChangeId("2")
            }
            .apply {
                assertThat(values().map { it.title }).containsExactly("", "response: 1", "response: 2")
            }
    }

    class DataRepo {
        data class Response(val id: String, val name: String)

        fun fetch(id: String) = RxStream.fromObservable(id) {
            Observable.just(Response(id = id, name = "response: $id"))
        }
    }

    object MyFormula {
        data class State(
            val selectedId: String? = null,
            val response: DataRepo.Response? = null
        )

        class RenderModel(
            val title: String,
            val onChangeId: (String) -> Unit
        )

        fun create(): Formula<Unit, State, RenderModel> {
            val dataRepo = DataRepo()
            return TestUtils.create(State()) { state, context ->
                Evaluation(
                    renderModel = RenderModel(
                        title = state.response?.name ?: "",
                        onChangeId = context.eventCallback { id ->
                            state.copy(selectedId = id).noMessages()
                        }
                    ),
                    updates = context.updates {
                        if (state.selectedId != null) {
                            events(dataRepo.fetch(state.selectedId)) { response ->
                                state.copy(response = response).noMessages()
                            }
                        }
                    }
                )
            }
        }
    }
}

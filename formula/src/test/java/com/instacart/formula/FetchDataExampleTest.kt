package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import io.reactivex.Observable
import org.junit.Test

class FetchDataExampleTest {

    @Test fun `fake network example`() {

        MyFormula()
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

    class MyFormula : Formula<Unit, MyFormula.State, MyFormula.RenderModel> {
        private val dataRepo = DataRepo()

        data class State(
            val selectedId: String? = null,
            val response: DataRepo.Response? = null
        )

        class RenderModel(
            val title: String,
            val onChangeId: (String) -> Unit
        )

        override fun initialState(input: Unit): State = State()

        override fun evaluate(
            input: Unit,
            state: State,
            context: FormulaContext<State>
        ): Evaluation<RenderModel> {
            return Evaluation(
                renderModel = RenderModel(
                    title = state.response?.name ?: "",
                    onChangeId = context.eventCallback { id ->
                        state.copy(selectedId = id).noEffects()
                    }
                ),
                updates = context.updates {
                    if (state.selectedId != null) {
                        events(dataRepo.fetch(state.selectedId)) { response ->
                            state.copy(response = response).noEffects()
                        }
                    }
                }
            )
        }
    }

}

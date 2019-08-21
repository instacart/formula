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
                assertThat(values().map { it.title }).containsExactly("", "response: 1", "response: 2")
            }
    }

    class FetchDataStream : RxStream<FetchDataStream.Request, FetchDataStream.Response> {
        data class Request(val dataId: String)

        data class Response(val id: String, val name: String)

        override fun observable(parameter: Request): Observable<Response> {
            return Observable.just(Response(id = parameter.dataId, name = "response: ${parameter.dataId}"))
        }
    }

    class MyFormula : Formula<Unit, MyFormula.State, MyFormula.RenderModel> {
        private val fetchStream = FetchDataStream()

        data class State(
            val selectedId: String? = null,
            val response: FetchDataStream.Response? = null
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
                        state.copy(selectedId = id).noMessages()
                    }
                ),
                updates = context.updates {
                    if (state.selectedId != null) {
                        events(fetchStream, FetchDataStream.Request(state.selectedId)) { response ->
                            state.copy(response = response).noMessages()
                        }
                    }
                }
            )
        }
    }

}

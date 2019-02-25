package com.instacart.mvi

import com.google.common.truth.Truth.assertThat
import com.instacart.client.mvi.state.ICNextReducers
import io.reactivex.Flowable
import io.reactivex.subscribers.TestSubscriber
import org.junit.Test

class JustReducerGeneratedCodeTest {

    @com.instacart.client.mvi.State(reducers = Reducers::class)
    data class State(
        val name: String = "",
        val password: String = ""
    )

    class Reducers : ICNextReducers<State, Unit>() {

        fun onNameChanged(name: String) = withoutEffects {
            it.copy(name = name)
        }
    }

    @Test
    fun simpleTest() {
        val events = JustReducerGeneratedCodeTestStateEvents(reducers = Reducers())
        val subscriber = TestSubscriber<State>()
        events
            .bind(onNameChanged = Flowable.just("John Doe"))
            .scan(State()) { acc, reducer ->
                reducer(acc).state
            }
            .subscribe(subscriber)

        assertThat(subscriber.valueCount()).isEqualTo(2)
        subscriber.assertValueAt(0, State())
        subscriber.assertValueAt(1, State(name = "John Doe"))
    }
}
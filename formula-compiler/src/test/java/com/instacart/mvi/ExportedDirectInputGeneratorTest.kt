package com.instacart.mvi

import com.google.common.truth.Truth
import com.instacart.client.mvi.ExportedProperty
import io.reactivex.subscribers.TestSubscriber
import org.junit.Test

class ExportedDirectInputGeneratorTest {

    @com.instacart.client.mvi.State
    data class State(
        @ExportedProperty(isDirectInput = true) val password: String
    )

    @Test
    fun singleExportedDirectInput() {
        val events = ExportedDirectInputGeneratorTestStateEvents()
        val changes = events.bind()
        val subscriber = TestSubscriber<State>()
        ICTestUtils.bind(State(password = ""), changes, subscriber)

        events.onPasswordChanged("my-password")

        Truth.assertThat(subscriber.values()).hasSize(2)
        Truth.assertThat(subscriber.values()[0].password).isEqualTo("")
        Truth.assertThat(subscriber.values()[1].password).isEqualTo("my-password")
    }
}
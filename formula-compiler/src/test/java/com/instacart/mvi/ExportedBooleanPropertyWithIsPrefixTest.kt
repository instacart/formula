package com.instacart.mvi

import com.google.common.truth.Truth.assertThat
import com.instacart.client.mvi.ExportedProperty
import io.reactivex.subscribers.TestSubscriber
import org.junit.Test

class ExportedBooleanPropertyWithIsPrefixTest {

    @com.instacart.client.mvi.State
    data class State(
        @ExportedProperty(isDirectInput = true) val isEnabled: Boolean = false
    )

    @Test
    fun primitiveProperty() {
        val events = ExportedBooleanPropertyWithIsPrefixTestStateEvents()
        val changes = events.bind()

        val state = State()
        val subscriber = TestSubscriber<State>()
        ICTestUtils.bind(state, changes, subscriber)

        events.onIsEnabledChanged(true)

        assertThat(subscriber.values()).hasSize(2)
        assertThat(subscriber.values()[0].isEnabled).isEqualTo(false)
        assertThat(subscriber.values()[1].isEnabled).isEqualTo(true)
    }
}
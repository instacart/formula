package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.annotations.ExportedProperty
import io.reactivex.Observable
import org.junit.Test

class SingleExportedPropertyTest {

    @com.instacart.formula.annotations.State
    data class State(
        @ExportedProperty val name: String
    )

    @Test
    fun singleExportedProperty() {
        val events = SingleExportedPropertyTestStateEvents()
        val changes = events.bind(
            onNameChanged = Observable.just("new-name")
        )

        val state = State(name = "initial-name")
        val subscriber = TestUtils.bind(state, changes)

        assertThat(subscriber.values()).hasSize(2)
        assertThat(subscriber.values()[0].name).isEqualTo("initial-name")
        assertThat(subscriber.values()[1].name).isEqualTo("new-name")
    }
}

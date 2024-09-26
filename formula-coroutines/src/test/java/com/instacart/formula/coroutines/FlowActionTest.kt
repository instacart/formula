package com.instacart.formula.coroutines

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

class FlowActionTest {

    @Test
    fun `default key is null`() {
        val action = FlowAction.fromFlow { flowOf("") }
        assertThat(action.key()).isNull()
    }

    @Test
    fun `specified key`() {
        val action = FlowAction.fromFlow("unique-key") { flowOf("") }
        assertThat(action.key()).isEqualTo("unique-key")
    }
}
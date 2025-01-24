package com.instacart.formula.coroutines

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.delay
import org.junit.Test

class SuspendActionTest {

    @Test
    fun `default key is null`() {
        val action = SuspendAction.from { delay(100) }
        assertThat(action.key()).isNull()
    }

    @Test
    fun `specified key`() {
        val action = SuspendAction.from("unique-key") { delay(100) }
        assertThat(action.key()).isEqualTo("unique-key")
    }
}
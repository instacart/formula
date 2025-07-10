package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RuntimeConfigTest {
    @Test
    fun `default dispatcher is null`() {
        val config = RuntimeConfig()
        assertThat(config.defaultDispatcher).isNull()
    }

    @Test
    fun `default inspector is null`() {
        val config = RuntimeConfig()
        assertThat(config.inspector).isNull()
    }
}
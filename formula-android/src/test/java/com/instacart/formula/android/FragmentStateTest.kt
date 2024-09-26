package com.instacart.formula.android

import com.google.common.truth.Truth
import com.instacart.formula.android.fakes.MainKey
import org.junit.Test

class FragmentStateTest {
    @Test fun `visibleOutput is null when visible ids are empty`() {
        val state = FragmentState(
            visibleIds = emptyList(),
        )

        Truth.assertThat(state.visibleOutput()).isNull()
    }

    @Test
    fun `visible output is null until output is set`() {
        val fragmentKey = MainKey(id = 1)
        val fragmentId = FragmentId(
            instanceId = "instanceId",
            key = fragmentKey
        )

        val state = FragmentState(
            visibleIds = listOf(fragmentId)
        )

        Truth.assertThat(state.visibleOutput()).isNull()
    }

    @Test
    fun `visible output is not null when visible fragment has an output`() {
        val fragmentKey = MainKey(id = 1)
        val fragmentId = FragmentId(
            instanceId = "instanceId",
            key = fragmentKey
        )

        val fragmentOutput = FragmentOutput(
            key = fragmentKey,
            renderModel = "value"
        )

        val state = FragmentState(
            visibleIds = listOf(fragmentId),
            outputs = mapOf(
                fragmentId to fragmentOutput
            )
        )

        Truth.assertThat(state.visibleOutput()).isEqualTo(fragmentOutput)
    }
}
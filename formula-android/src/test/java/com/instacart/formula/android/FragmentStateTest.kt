package com.instacart.formula.android

import com.google.common.truth.Truth
import com.instacart.formula.android.fakes.MainKey
import org.junit.Test

class FragmentStateTest {
    @Test fun `visibleOutput is null when visible ids are empty`() {
        val state = NavigationState(
            visibleIds = emptyList(),
        )

        Truth.assertThat(state.visibleOutput()).isNull()
    }

    @Test
    fun `visible output is null until output is set`() {
        val fragmentKey = MainKey(id = 1)
        val routeId = RouteId(
            instanceId = "instanceId",
            key = fragmentKey
        )

        val state = NavigationState(
            visibleIds = listOf(routeId)
        )

        Truth.assertThat(state.visibleOutput()).isNull()
    }

    @Test
    fun `visible output is not null when visible fragment has an output`() {
        val fragmentKey = MainKey(id = 1)
        val routeId = RouteId(
            instanceId = "instanceId",
            key = fragmentKey
        )

        val routeOutput = RouteOutput(
            key = fragmentKey,
            renderModel = "value"
        )

        val state = NavigationState(
            visibleIds = listOf(routeId),
            outputs = mapOf(
                routeId to routeOutput
            )
        )

        Truth.assertThat(state.visibleOutput()).isEqualTo(routeOutput)
    }
}
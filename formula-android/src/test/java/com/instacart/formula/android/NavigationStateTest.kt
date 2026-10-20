package com.instacart.formula.android

import com.google.common.truth.Truth
import com.instacart.formula.android.fakes.MainKey
import org.junit.Test

class NavigationStateTest {
    @Test fun `visibleOutput is null when visible ids are empty`() {
        val state = NavigationState(
            visibleIds = emptyList(),
        )

        Truth.assertThat(state.visibleOutput()).isNull()
    }

    @Test
    fun `visible output is null until route output is set`() {
        val routeKey = MainKey(id = 1)
        val routeId = RouteId(
            instanceId = "instanceId",
            key = routeKey
        )

        val state = NavigationState(
            visibleIds = listOf(routeId)
        )

        Truth.assertThat(state.visibleOutput()).isNull()
    }

    @Test
    fun `visible output is not null when the visible route has output`() {
        val routeKey = MainKey(id = 1)
        val routeId = RouteId(
            instanceId = "instanceId",
            key = routeKey
        )

        val routeOutput = RouteOutput(
            key = routeKey,
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

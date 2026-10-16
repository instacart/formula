package com.instacart.formula.android

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.fakes.MainKey
import org.junit.Test

class NavigationStateTest {
    @Test
    fun `visible output is null when visible route ids are empty`() {
        val state = NavigationState(visibleIds = emptyList())

        assertThat(state.visibleOutput()).isNull()
    }

    @Test
    fun `visible output is null until output is set`() {
        val routeKey = MainKey(id = 1)
        val routeId = RouteId(instanceId = "instanceId", key = routeKey)
        val state = NavigationState(visibleIds = listOf(routeId))

        assertThat(state.visibleOutput()).isNull()
    }

    @Test
    fun `visible output is not null when visible route has an output`() {
        val routeKey = MainKey(id = 1)
        val routeId = RouteId(instanceId = "instanceId", key = routeKey)
        val routeOutput = RouteOutput(key = routeKey, renderModel = "value")
        val state = NavigationState(
            visibleIds = listOf(routeId),
            outputs = mapOf(routeId to routeOutput),
        )

        assertThat(state.visibleOutput()).isEqualTo(routeOutput)
    }
}

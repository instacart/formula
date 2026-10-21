package com.instacart.formula.android

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.events.RouteLifecycleEvent
import com.instacart.formula.android.fakes.MainKey
import org.junit.Test

class RouteModelsTest {

    @Test fun routeId() {
        val routeKey = MainKey(id = 1)
        val routeId = RouteId(
            instanceId = "instanceId",
            key = routeKey
        )

        assertThat(routeId.instanceId).isEqualTo("instanceId")
        assertThat(routeId.key).isEqualTo(routeKey)
    }

    @Test fun routeOutput() {
        val key = MainKey(id = 1)
        val output = RouteOutput(
            key = key,
            renderModel = Unit
        )
        assertThat(output.key).isEqualTo(key)
        assertThat(output.renderModel).isEqualTo(Unit)
    }

    @Test fun routeLifecycleEventRemoved() {
        val routeKey = MainKey(id = 1)
        val routeId = RouteId(
            instanceId = "instanceId",
            key = routeKey
        )

        val event = RouteLifecycleEvent.Removed(
            routeId = routeId,
            lastState = "last-state"
        )
        assertThat(event.routeId).isEqualTo(routeId)
        assertThat(event.lastState).isEqualTo("last-state")
    }
}

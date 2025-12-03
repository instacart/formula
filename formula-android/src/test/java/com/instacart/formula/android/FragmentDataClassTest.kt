package com.instacart.formula.android

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.events.ActivityResult
import com.instacart.formula.android.events.RouteLifecycleEvent
import com.instacart.formula.android.fakes.MainKey
import org.junit.Test

class FragmentDataClassTest {

    @Test fun fragmentId() {
        val fragmentKey = MainKey(id = 1)
        val routeId = RouteId(
            instanceId = "instanceId",
            key = fragmentKey
        )

        assertThat(routeId.instanceId).isEqualTo("instanceId")
        assertThat(routeId.key).isEqualTo(fragmentKey)
    }

    @Test fun fragmentOutput() {
        val key = MainKey(id = 1)
        val output = RouteOutput(
            key = key,
            renderModel = Unit
        )
        assertThat(output.key).isEqualTo(key)
        assertThat(output.renderModel).isEqualTo(Unit)
    }

    @Test fun activityResult() {
        val result = ActivityResult(
            requestCode = 0,
            resultCode = 1,
            data = null
        )
        assertThat(result.requestCode).isEqualTo(0)
        assertThat(result.resultCode).isEqualTo(1)
        assertThat(result.data).isNull()
    }

    @Test fun fragmentLifecycleEventRemoved() {
        val fragmentKey = MainKey(id = 1)
        val routeId = RouteId(
            instanceId = "instanceId",
            key = fragmentKey
        )

        val event = RouteLifecycleEvent.Removed(
            routeId = routeId,
            lastState = "last-state"
        )
        assertThat(event.routeId).isEqualTo(routeId)
        assertThat(event.lastState).isEqualTo("last-state")
    }
}
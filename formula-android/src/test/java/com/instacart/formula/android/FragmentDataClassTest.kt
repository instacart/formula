package com.instacart.formula.android

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.events.ActivityResult
import com.instacart.formula.android.events.FragmentLifecycleEvent
import com.instacart.formula.android.fakes.MainKey
import org.junit.Test

class FragmentDataClassTest {

    @Test fun fragmentId() {
        val fragmentKey = MainKey(id = 1)
        val fragmentId = FragmentId(
            instanceId = "instanceId",
            key = fragmentKey
        )

        assertThat(fragmentId.instanceId).isEqualTo("instanceId")
        assertThat(fragmentId.key).isEqualTo(fragmentKey)
    }

    @Test fun fragmentOutput() {
        val key = MainKey(id = 1)
        val output = FragmentOutput(
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
        val fragmentId = FragmentId(
            instanceId = "instanceId",
            key = fragmentKey
        )

        val event = FragmentLifecycleEvent.Removed(
            fragmentId = fragmentId,
            lastState = "last-state"
        )
        assertThat(event.fragmentId).isEqualTo(fragmentId)
        assertThat(event.lastState).isEqualTo("last-state")
    }
}
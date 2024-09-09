package com.instacart.formula.android

import com.google.common.truth.Truth
import com.instacart.formula.android.fakes.MainKey
import org.junit.Test

class FragmentDataClassTest {

    @Test fun fragmentId() {
        val fragmentKey = MainKey(id = 1)
        val fragmentId = FragmentId(
            instanceId = "instanceId",
            key = fragmentKey
        )

        Truth.assertThat(fragmentId.instanceId).isEqualTo("instanceId")
        Truth.assertThat(fragmentId.key).isEqualTo(fragmentKey)
    }

    @Test fun fragmentOutput() {
        val key = MainKey(id = 1)
        val output = FragmentOutput(
            key = key,
            renderModel = Unit
        )
        Truth.assertThat(output.key).isEqualTo(key)
        Truth.assertThat(output.renderModel).isEqualTo(Unit)
    }
}
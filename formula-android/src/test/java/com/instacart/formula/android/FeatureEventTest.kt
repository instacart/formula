package com.instacart.formula.android

import com.google.common.truth.Truth
import com.instacart.formula.android.fakes.MainKey
import org.junit.Test
import java.lang.RuntimeException

class FeatureEventTest {

    @Test fun failureEvent() {
        val fragmentId = FragmentId(
            instanceId = "random",
            key = MainKey(id = 100)
        )

        val error = RuntimeException("error")
        val event = FeatureEvent.Failure(fragmentId, error)
        Truth.assertThat(event.id).isEqualTo(fragmentId)
        Truth.assertThat(event.error).isEqualTo(error)
    }
}
package com.instacart.formula.test

import com.google.common.truth.Truth
import org.junit.Test

class TestCallbackTest {

    @Test
    fun `assertTimesCalled throws an exception when count does not match`() {
        val callback = TestCallback()
        callback.assertTimesCalled(0)
        callback.invoke()
        callback.assertTimesCalled(1)

        val result = kotlin.runCatching { callback.assertTimesCalled(5) }
        Truth.assertThat(result.exceptionOrNull()).hasMessageThat().contains(
            "Expected: 5, was: 1"
        )
    }
}
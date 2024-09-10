package com.instacart.formula.test

import com.google.common.truth.Truth
import org.junit.Test

class TestListenerTest {

    @Test
    fun `assertTimesCalled throws an exception when count does not match`() {
        val listener = TestListener<String>()
        listener.assertTimesCalled(0)
        listener.invoke("value")
        listener.assertTimesCalled(1)
        listener.invoke("second")
        listener.assertTimesCalled(2)

        val result = runCatching { listener.assertTimesCalled(5) }
        Truth.assertThat(result.exceptionOrNull()).hasMessageThat().contains(
            "Expected: 5, was: 2"
        )
    }

    @Test
    fun values() {
        val listener = TestListener<String>()
        listener.invoke("value")
        listener.invoke("second")
        listener.invoke("third")

        Truth.assertThat(listener.values()).containsExactly(
            "value", "second", "third"
        ).inOrder()
    }
}
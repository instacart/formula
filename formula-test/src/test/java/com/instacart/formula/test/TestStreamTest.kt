package com.instacart.formula.test

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Cancelable
import com.instacart.formula.Stream
import org.junit.Test
import java.lang.IllegalStateException

class TestStreamTest {

    @Test fun `assert values success`() {
        multipleValueStream().test().assertValues(1, 2)
    }

    @Test fun `assert value fails due to different size`() {
        val exception = fails { multipleValueStream().test().assertValues(1) }
        assertThat(exception).isInstanceOf(AssertionError::class.java)
    }

    @Test fun `assert value fails due to different value`() {
        val exception = fails { multipleValueStream().test().assertValues(1, 5) }
        assertThat(exception).isInstanceOf(AssertionError::class.java)
    }

    inline fun fails(action: () -> Unit): Throwable {
        try {
            action()
        } catch (t: Exception) {
            return t
        }

        throw IllegalStateException("Action succeeded.")
    }

    fun multipleValueStream() = object : Stream<Int> {
        override fun start(send: (Int) -> Unit): Cancelable? {
            send(1)
            send(2)
            return null
        }

        override fun key(): Any = Unit
    }
}

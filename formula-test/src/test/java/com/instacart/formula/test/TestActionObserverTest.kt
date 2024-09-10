package com.instacart.formula.test

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import org.junit.Test
import java.lang.IllegalStateException

class TestActionObserverTest {

    @Test fun `assert values success`() {
        multipleValueStream().test().assertValues(1, 2)
    }

    @Test fun `assert value fails due to different size`() {
        val result = runCatching { multipleValueStream().test().assertValues(1) }
        assertThat(result.exceptionOrNull()).isInstanceOf(AssertionError::class.java)
    }

    @Test fun `assert value fails due to different value`() {
        val result = runCatching { multipleValueStream().test().assertValues(1, 5) }
        assertThat(result.exceptionOrNull()).isInstanceOf(AssertionError::class.java)
    }

    @Test fun values() {
        val values = multipleValueStream().test().values()
        assertThat(values).hasSize(2)
    }

    @Test fun `cancel throws exception if action does not provide cancelable`() {
        val result = kotlin.runCatching { multipleValueStream().test().cancel() }
        assertThat(result.exceptionOrNull()).hasMessageThat().contains(
            "Action did not return a cancelable."
        )
    }

    @Test fun `cancel invokes cancelable`() {
        var cancelableCalled = 0
        val action = object : Action<String> {
            override fun start(send: (String) -> Unit): Cancelable {
                return Cancelable { cancelableCalled += 1 }
            }

            override fun key(): Any? = null
        }

        action.test().cancel()
        assertThat(cancelableCalled).isEqualTo(1)
    }

    fun multipleValueStream() = object : Action<Int> {
        override fun start(send: (Int) -> Unit): Cancelable? {
            send(1)
            send(2)
            return null
        }

        override fun key(): Any = Unit
    }
}

package com.instacart.formula.test

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TestActionObserverTest {

    @Test fun `assert values success`() = runTest {
        multipleValueStream().test {
            assertValues(1, 2)
        }
    }

    @Test fun `assert value fails due to different size`() = runTest {
        val result = runCatching {
            multipleValueStream().test { assertValues(1) }
        }
        assertThat(result.exceptionOrNull()).isInstanceOf(AssertionError::class.java)
    }

    @Test fun `assert value fails due to different value`() = runTest {
        val result = runCatching {
            multipleValueStream().test { assertValues(1, 5) }
        }
        assertThat(result.exceptionOrNull()).isInstanceOf(AssertionError::class.java)
    }

    @Test fun values() = runTest {
        multipleValueStream().test {
            assertThat(values()).hasSize(2)
        }
    }

    @Test fun `cancel throws exception if action does not provide cancelable`() = runTest {
        val result = kotlin.runCatching {
            multipleValueStream().test { cancel() }
        }
        assertThat(result.exceptionOrNull()).hasMessageThat().contains(
            "Action did not return a cancelable."
        )
    }

    @Test fun `cancel invokes cancelable`() = runTest {
        var cancelableCalled = 0
        val action = object : Action<String> {
            override fun start(scope: CoroutineScope, send: (String) -> Unit): Cancelable {
                return Cancelable { cancelableCalled += 1 }
            }

            override fun key(): Any? = null
        }

        action.test {
            cancel()
            assertThat(cancelableCalled).isEqualTo(1)
        }
    }

    private fun multipleValueStream() = object : Action<Int> {
        override fun start(scope: CoroutineScope, send: (Int) -> Unit): Cancelable? {
            send(1)
            send(2)
            return null
        }

        override fun key(): Any = Unit
    }
}

package com.instacart.formula.coroutines

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Test

class CoroutineActionTest {

    @Test fun launch() {
        val action = CoroutineAction.launch { "Result" }
        assertThat(action.key()).isNull()

        val observer = action.test()
        observer.assertValues("Result")
    }

    @Test fun `launch - key`() {
        val action = CoroutineAction.launch("key") { "Result" }
        assertThat(action.key()).isEqualTo("key")

        val observer = action.test()
        observer.assertValues("Result")
    }

    @Test fun `launch - dispatcher`() {
        val dispatcher = StandardTestDispatcher()
        val action = CoroutineAction.launch(coroutineDispatcher = dispatcher) {
            delay(1000)
            "Result"
        }

        val observer = action.test()
        dispatcher.scheduler.advanceUntilIdle()
        observer.assertValues("Result")
    }

    @Test fun `launch - key, dispatcher`() {
        val dispatcher = StandardTestDispatcher()
        val action = CoroutineAction.launch("key", dispatcher) {
            delay(1000)
            "Result"
        }

        assertThat(action.key()).isEqualTo("key")

        val observer = action.test()
        dispatcher.scheduler.advanceUntilIdle()
        observer.assertValues("Result")
    }

    @Test fun launchCatching() {
        val action = CoroutineAction.launchCatching { throw RuntimeException("My error") }
        assertThat(action.key()).isNull()

        val observer = action.test()
        val value = observer.values()[0].exceptionOrNull()
        assertThat(value).hasMessageThat().isEqualTo("My error")
    }

    @Test fun `launchCatching - key`() {
        val action = CoroutineAction.launchCatching("key") { throw RuntimeException("My error") }
        assertThat(action.key()).isEqualTo("key")

        val observer = action.test()
        val value = observer.values()[0].exceptionOrNull()
        assertThat(value).hasMessageThat().isEqualTo("My error")
    }

    @Test fun `launchCatching - dispatcher`() {
        val dispatcher = StandardTestDispatcher()
        val action = CoroutineAction.launchCatching(dispatcher) {
            delay(1000)
            throw RuntimeException("My error")
        }
        val observer = action.test()
        dispatcher.scheduler.advanceUntilIdle()
        val value = observer.values()[0].exceptionOrNull()
        assertThat(value).hasMessageThat().isEqualTo("My error")
    }

    @Test fun `launchCatching - key, dispatcher`() {
        val dispatcher = StandardTestDispatcher()
        val action = CoroutineAction.launchCatching("key", dispatcher) {
            delay(1000)
            throw RuntimeException("My error")
        }
        assertThat(action.key()).isEqualTo("key")

        val observer = action.test()
        dispatcher.scheduler.advanceUntilIdle()
        val value = observer.values()[0].exceptionOrNull()
        assertThat(value).hasMessageThat().isEqualTo("My error")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `launchCatching - cancellation`() {
        val dispatcher = StandardTestDispatcher()
        val action = CoroutineAction.launchCatching("key", dispatcher) {
            delay(1000)
            throw RuntimeException("My error")
        }
        assertThat(action.key()).isEqualTo("key")

        val observer = action.test()
        dispatcher.scheduler.advanceTimeBy(500)
        observer.cancel()
        observer.assertValues()
    }
}
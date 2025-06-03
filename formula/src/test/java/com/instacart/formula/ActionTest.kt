package com.instacart.formula

import com.google.common.truth.Truth
import com.instacart.formula.test.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Test

class ActionTest {

    @Test
    fun launch() {
        val action = Action.launch { "Result" }
        Truth.assertThat(action.key()).isNull()

        val observer = action.test()
        observer.assertValues("Result")
    }

    @Test
    fun `launch - key`() {
        val action = Action.launch("key") { "Result" }
        Truth.assertThat(action.key()).isEqualTo("key")

        val observer = action.test()
        observer.assertValues("Result")
    }

    @Test
    fun `launch - dispatcher`() {
        val dispatcher = StandardTestDispatcher()
        val action = Action.launch(context = dispatcher) {
            delay(1000)
            "Result"
        }

        val observer = action.test()
        observer.assertValues()

        dispatcher.scheduler.advanceUntilIdle()
        observer.assertValues("Result")
    }

    @Test
    fun `launch - key, dispatcher`() {
        val dispatcher = StandardTestDispatcher()
        val action = Action.launch("key", dispatcher) {
            delay(1000)
            "Result"
        }

        Truth.assertThat(action.key()).isEqualTo("key")

        val observer = action.test()
        observer.assertValues()

        dispatcher.scheduler.advanceUntilIdle()
        observer.assertValues("Result")
    }

    @Test
    fun launchCatching() {
        val action = Action.launchCatching { throw RuntimeException("My error") }
        Truth.assertThat(action.key()).isNull()

        val observer = action.test()
        val value = observer.values()[0].exceptionOrNull()
        Truth.assertThat(value).hasMessageThat().isEqualTo("My error")
    }

    @Test
    fun `launchCatching - key`() {
        val action = Action.launchCatching("key") { throw RuntimeException("My error") }
        Truth.assertThat(action.key()).isEqualTo("key")

        val observer = action.test()
        val value = observer.values()[0].exceptionOrNull()
        Truth.assertThat(value).hasMessageThat().isEqualTo("My error")
    }

    @Test
    fun `launchCatching - dispatcher`() {
        val dispatcher = StandardTestDispatcher()
        val action = Action.launchCatching(dispatcher) {
            delay(1000)
            throw RuntimeException("My error")
        }
        val observer = action.test()
        observer.assertValues()

        dispatcher.scheduler.advanceUntilIdle()
        val value = observer.values()[0].exceptionOrNull()
        Truth.assertThat(value).hasMessageThat().isEqualTo("My error")
    }

    @Test
    fun `launchCatching - key, dispatcher`() {
        val dispatcher = StandardTestDispatcher()
        val action = Action.launchCatching("key", dispatcher) {
            delay(1000)
            throw RuntimeException("My error")
        }
        Truth.assertThat(action.key()).isEqualTo("key")

        val observer = action.test()
        observer.assertValues()

        dispatcher.scheduler.advanceUntilIdle()
        val value = observer.values()[0].exceptionOrNull()
        Truth.assertThat(value).hasMessageThat().isEqualTo("My error")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `launchCatching - cancellation`() {
        val dispatcher = StandardTestDispatcher()
        val action = Action.launchCatching("key", dispatcher) {
            delay(1000)
            throw RuntimeException("My error")
        }
        Truth.assertThat(action.key()).isEqualTo("key")

        val observer = action.test()
        observer.assertValues()

        dispatcher.scheduler.advanceTimeBy(500)
        observer.cancel()

        dispatcher.scheduler.advanceUntilIdle()
        observer.assertValues()
    }

    @Test
    fun `fromFlow - default key is null`() {
        val action = Action.fromFlow { flowOf("") }
        Truth.assertThat(action.key()).isNull()
    }

    @Test
    fun `fromFlow - specified key`() {
        val action = Action.fromFlow("unique-key") { flowOf("") }
        Truth.assertThat(action.key()).isEqualTo("unique-key")
    }
}
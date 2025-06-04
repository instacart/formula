package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Test

class ActionTest {

    @Test
    fun launch() = runTest {
        val action = Action.launch { "Result" }
        assertThat(action.key()).isNull()

        action.test {
            assertValues("Result")
        }
    }

    @Test
    fun `launch - key`() = runTest {
        val action = Action.launch("key") { "Result" }
        assertThat(action.key()).isEqualTo("key")

        action.test {
            assertValues("Result")
        }
    }

    @Test
    fun `launch - dispatcher`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val action = Action.launch(context = dispatcher) {
            delay(1000)
            "Result"
        }

        action.test {
            assertValues()

            dispatcher.scheduler.advanceUntilIdle()
            assertValues("Result")
        }
    }

    @Test
    fun `launch - key, dispatcher`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val action = Action.launch("key", dispatcher) {
            delay(1000)
            "Result"
        }

        assertThat(action.key()).isEqualTo("key")

        action.test {
            assertValues()

            dispatcher.scheduler.advanceUntilIdle()
            assertValues("Result")
        }
    }

    @Test
    fun launchCatching() = runTest {
        val action = Action.launchCatching { throw RuntimeException("My error") }
        assertThat(action.key()).isNull()

        action.test {
            val value = values()[0].exceptionOrNull()
            assertThat(value).hasMessageThat().isEqualTo("My error")
        }
    }

    @Test
    fun `launchCatching - key`() = runTest {
        val action = Action.launchCatching("key") { throw RuntimeException("My error") }
        assertThat(action.key()).isEqualTo("key")

        action.test {
            val value = values()[0].exceptionOrNull()
            assertThat(value).hasMessageThat().isEqualTo("My error")
        }
    }

    @Test
    fun `launchCatching - dispatcher`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val action = Action.launchCatching(dispatcher) {
            delay(1000)
            throw RuntimeException("My error")
        }
        action.test {
            assertValues()

            dispatcher.scheduler.advanceUntilIdle()
            val value = values()[0].exceptionOrNull()
            assertThat(value).hasMessageThat().isEqualTo("My error")
        }
    }

    @Test
    fun `launchCatching - key, dispatcher`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val action = Action.launchCatching("key", dispatcher) {
            delay(1000)
            throw RuntimeException("My error")
        }
        assertThat(action.key()).isEqualTo("key")

        action.test {
            assertValues()

            dispatcher.scheduler.advanceUntilIdle()
            val value = values()[0].exceptionOrNull()
            assertThat(value).hasMessageThat().isEqualTo("My error")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `launchCatching - cancellation`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val action = Action.launchCatching("key", dispatcher) {
            delay(1000)
            throw RuntimeException("My error")
        }
        assertThat(action.key()).isEqualTo("key")

        action.test {
            assertValues()

            dispatcher.scheduler.advanceTimeBy(500)
            cancel()

            dispatcher.scheduler.advanceUntilIdle()
            assertValues()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `launch - delay`() = runTest {
        Action.launch {
            delay(500)
            "result"
        }.test {
            assertValues()

            testScheduler.advanceTimeBy(501)
            assertValues("result")
        }
    }

    @Test
    fun `fromFlow - default key is null`() {
        val action = Action.fromFlow { flowOf("") }
        assertThat(action.key()).isNull()
    }

    @Test
    fun `fromFlow - specified key`() {
        val action = Action.fromFlow("unique-key") { flowOf("") }
        assertThat(action.key()).isEqualTo("unique-key")
    }

    @Test
    fun `fromFlow - with relay`() = runTest {
        val sharedFlow = MutableSharedFlow<Int>(
            replay = 0,
            extraBufferCapacity = Int.MAX_VALUE,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

        Action.fromFlow { sharedFlow }.test {
            assertValues()

            sharedFlow.forceEmit(1)
            assertValues(1)

            sharedFlow.forceEmit(2)
            assertValues(1, 2)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun <T> MutableSharedFlow<T>.forceEmit(value: T) {
        GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
            withContext(Dispatchers.Unconfined) {
                emit(value)
            }
        }
    }
}

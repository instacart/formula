package com.instacart.formula.integration

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import org.junit.Before
import org.junit.Test

class FlowReducersTest {
    data class TestKey(val value: String)

    lateinit var root: Binding<Unit, TestKey>
    lateinit var reducers: FlowReducers<TestKey>

    @Before fun setup() {
        root = mock()
        reducers = FlowReducers(root)
    }

    @Test fun `on backstack change we clear detached contracts`() {
        val firstEntry = TestKey("first")
        val secondEntry = TestKey("second")

        val initialStack = BackStack(listOf(firstEntry, secondEntry))
        val backstackEvent = BackStack(listOf(firstEntry))
        val newState = reducers.onBackstackChange(backstackEvent).invoke(
            FlowState(
                backStack = initialStack,
                states = mapOf(
                    firstEntry to KeyState(firstEntry, "state"),
                    secondEntry to KeyState(secondEntry, "second state")
                )
            )
        )

        assertThat(newState.states).hasSize(1)
        assertThat(newState.states[firstEntry]).isNotNull()
    }
}

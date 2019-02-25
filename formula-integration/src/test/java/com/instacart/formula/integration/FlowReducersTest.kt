package com.instacart.formula.integration

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import org.junit.Before
import org.junit.Test

class FlowReducersTest {
    data class TestKey(val value: String)

    lateinit var root: KeyBinding.CompositeBinding<Unit, TestKey, Unit>
    lateinit var reducers: FlowReducers<TestKey>

    @Before fun setup() {
        root = mock()
        reducers = FlowReducers(root)
    }

    @Test fun onBackstackChange_clearDetachedContracts() {
        val firstEntry = TestKey("first")
        val secondEntry = TestKey("second")

        val initialStack = ActiveKeys(listOf(firstEntry, secondEntry))
        val backstackEvent = ActiveKeys(listOf(firstEntry))
        val newState = reducers.onBackstackChange(backstackEvent).invoke(
            FlowState(
                activeKeys = initialStack,
                contracts = mapOf(
                    firstEntry to KeyState(firstEntry, "state"),
                    secondEntry to KeyState(secondEntry, "second state")
                )
            )
        )

        assertThat(newState.contracts).hasSize(1)
        assertThat(newState.contracts[firstEntry]).isNotNull()
    }
}

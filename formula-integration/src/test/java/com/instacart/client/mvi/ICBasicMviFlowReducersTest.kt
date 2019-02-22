package com.instacart.client.mvi

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.ICMviState
import com.nhaarman.mockito_kotlin.mock
import org.junit.Before
import org.junit.Test

class ICBasicMviFlowReducersTest {
    data class TestKey(val value: String)

    lateinit var root: ICMviBinding.CompositeBinding<Unit, TestKey, Unit>
    lateinit var reducers: ICBasicMviFlowReducers<TestKey>

    @Before fun setup() {
        root = mock()
        reducers = ICBasicMviFlowReducers(root)
    }

    @Test fun onBackstackChange_clearDetachedContracts() {
        val firstEntry = TestKey("first")
        val secondEntry = TestKey("second")

        val initialStack = ICActiveMviKeys(listOf(firstEntry, secondEntry))
        val backstackEvent = ICActiveMviKeys(listOf(firstEntry))
        val newState = reducers.onBackstackChange(backstackEvent).invoke(
            ICBasicMviFlowState(
                activeKeys = initialStack,
                contracts = mapOf(
                    firstEntry to ICMviState(firstEntry, "state"),
                    secondEntry to ICMviState(secondEntry, "second state")
                )
            )
        )

        assertThat(newState.contracts).hasSize(1)
        assertThat(newState.contracts[firstEntry]).isNotNull()
    }
}

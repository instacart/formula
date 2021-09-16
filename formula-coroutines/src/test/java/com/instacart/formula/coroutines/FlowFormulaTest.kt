package com.instacart.formula.coroutines

import com.google.common.truth.Truth
import com.instacart.formula.test.test
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.junit.Rule
import org.junit.Test

@ExperimentalStdlibApi
class FlowFormulaTest : CoroutineTest {

    @get:Rule
    override val coroutineRule = CoroutineTestRule()

    @Test
    fun `initial value`() = test {
        TestFlowFormula()
            .test("initial", this)
            .apply {
                Truth.assertThat(values()).containsExactly(0).inOrder()
            }
    }

    @Test
    fun `initial value and subsequent events from relay`() = test {
        TestFlowFormula()
            .test("initial", this)
            .apply {
                formula.sharedFlow.tryEmit(1)
                formula.sharedFlow.tryEmit(2)
                formula.sharedFlow.tryEmit(3)
            }
            .apply {
                Truth.assertThat(values()).containsExactly(0, 1, 2, 3).inOrder()
            }
    }

    @Test
    fun `new input restarts formula`() = test {
        TestFlowFormula()
            .test(this)
            .input("initial")
            .apply { formula.sharedFlow.tryEmit(1) }
            .input("reset")
            .apply { formula.sharedFlow.tryEmit(1) }
            .apply {
                Truth.assertThat(values()).containsExactly(0, 1, 0, 1).inOrder()
            }
    }

    internal class TestFlowFormula : FlowFormula<String, Int>() {
       
        val sharedFlow =
            MutableSharedFlow<Int>(0, extraBufferCapacity = 1, BufferOverflow.DROP_OLDEST)

        override fun initialValue(input: String): Int = 0

        override fun flow(input: String): Flow<Int> = sharedFlow.asSharedFlow()
    }
}

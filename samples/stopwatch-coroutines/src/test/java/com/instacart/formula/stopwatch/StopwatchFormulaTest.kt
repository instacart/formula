package com.instacart.formula.stopwatch

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.coroutines.toFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.withIndex
import org.junit.Rule
import org.junit.Test

@ExperimentalStdlibApi
class StopwatchFormulaTest : CoroutineTest {

    @get:Rule
    override val coroutineRule = CoroutineTestRule()

    @Test
    fun `increment 5 times`() = test {

        StopwatchFormula(this)
            .toFlow()
            .withIndex()
            .onEach {
                val (index, value) = it
                if (index == 5) {
                    assertThat(value.timePassed).isEqualTo("0s 05")

                }
            }.launchIn(this)

    }
}

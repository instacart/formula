package com.instacart.formula.stopwatch

import com.instacart.formula.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StopwatchFormulaTest {

    // TODO:
    @Test fun `increment 5 times`() = runTest {
        StopwatchFormula().test(coroutineContext).input(Unit)
    }
}

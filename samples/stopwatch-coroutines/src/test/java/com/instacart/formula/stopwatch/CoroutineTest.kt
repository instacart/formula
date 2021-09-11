package com.instacart.formula.stopwatch

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule

@ExperimentalCoroutinesApi
interface CoroutineTest {

    @ExperimentalStdlibApi
    @get:Rule
    val coroutineRule: CoroutineTestRule

    @ExperimentalStdlibApi
    fun test(
        test: suspend TestCoroutineScope.() -> Unit) = coroutineRule.testCoroutineScope.runBlockingTest { test(coroutineRule.testCoroutineScope) }

}
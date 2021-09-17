package com.instacart.formula.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
class CoroutineTestRule(val testCoroutineScope: TestCoroutineScope = TestCoroutineScope(TestCoroutineDispatcher())) : TestWatcher() {
    init {
        Dispatchers.setMain(testCoroutineScope.coroutineContext[CoroutineDispatcher]!!)

    }

    override fun finished(description: Description?) {
        super.finished(description)
        Dispatchers.resetMain()
        testCoroutineScope.cleanupTestCoroutines()
    }
}
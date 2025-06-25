package com.instacart.formula.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instacart.testutils.android.StateFlowKey
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StateFlowFeatureTest {

    @Test fun `non-async initialization`() {
        with(StateFlowFeatureRobot()) {
            add(StateFlowKey(initAsync = false))
            assertHasAsyncTasks(expected = 0)

            assertValue("")
            publishValue("new-value")
            assertValue("new-value")
        }
    }

    @Test fun `async initialization`() = runTest {
        with(StateFlowFeatureRobot()) {
            add(StateFlowKey(initAsync = true))
            assertHasAsyncTasks(expected = 1)
            executeTasks()

            assertValue("")
            publishValue("new-value")
            assertValue("new-value")
        }
    }
}
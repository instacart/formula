package com.instacart.formula.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.instacart.formula.android.fakes.StateFlowFeatureFactory
import com.instacart.formula.android.fakes.StateFlowFormula
import com.instacart.formula.android.fakes.asAddedEvent
import com.instacart.testutils.android.StateFlowKey
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import java.io.Closeable
import java.util.concurrent.Executor

@RunWith(AndroidJUnit4::class)
class StateFlowFeatureTest {

    @Test fun `non-async initialization`() {
        StateFlowFeatureRobot().use { robot ->
            robot.add(StateFlowKey(initAsync = false))
            robot.assertHasAsyncTasks(expected = 0)

            robot.assertValue("")
            robot.publishValue("new-value")
            robot.assertValue("new-value")
        }
    }

    @Test fun `async initialization`() = runTest {
        StateFlowFeatureRobot().use { robot ->
            robot.add(StateFlowKey(initAsync = true))
            robot.assertHasAsyncTasks(expected = 1)
            robot.executeTasks()

            robot.assertValue("")
            robot.publishValue("new-value")
            robot.assertValue("new-value")
        }
    }

    private class StateFlowFeatureRobot : Closeable {
        private val formula = StateFlowFormula()

        private val tasks = mutableListOf<Runnable>()
        private val executors = Executor { command -> tasks.add(command) }

        private val store = NavigationStore.Builder()
            .setRouteEnvironment(
                environment = RouteEnvironment(
                    asyncCoroutineDispatcher = executors.asCoroutineDispatcher(),
                )
            )
            .build {
                val featureFactory = StateFlowFeatureFactory(formula)
                bind(featureFactory)
            }

        fun add(key: StateFlowKey) = apply {
            store.onLifecycleEvent(key.asAddedEvent())
        }

        fun executeTasks() = apply {
            tasks.forEach { it.run() }
        }

        fun publishValue(value: String, instanceId: String = "") = apply {
            formula.publish(instanceId, value)
        }

        fun assertValue(expected: String) = apply {
            Truth.assertThat(getValue()).isEqualTo(expected)
        }

        fun assertHasAsyncTasks(expected: Int) = apply {
            Truth.assertThat(tasks).hasSize(expected)
        }

        private fun getValue(): String {
            return store.stateFlow.value.outputs.values.first().renderModel as String
        }

        override fun close() {
            store.dispose()
        }
    }
}
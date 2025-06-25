package com.instacart.formula.android

import com.google.common.truth.Truth
import com.instacart.formula.android.fakes.StateFlowFeatureFactory
import com.instacart.formula.android.fakes.StateFlowFormula
import com.instacart.formula.android.fakes.asAddedEvent
import com.instacart.testutils.android.StateFlowKey
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executor
import kotlin.math.exp

class StateFlowFeatureRobot {
    private val formula = StateFlowFormula()

    private val tasks = mutableListOf<Runnable>()
    private val executors = Executor { command -> tasks.add(command) }

    private val store = FragmentStore.Builder()
        .setAsyncCoroutineDispatcher(executors.asCoroutineDispatcher())
        .build {
            val featureFactory = StateFlowFeatureFactory(formula)
            bind(featureFactory)
        }

    private val observer = store.state().test()

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
        val last = observer.values().last()
        return last.outputs.values.first().renderModel as String
    }
}
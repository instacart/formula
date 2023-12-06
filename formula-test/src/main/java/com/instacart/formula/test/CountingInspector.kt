package com.instacart.formula.test

import com.instacart.formula.DeferredAction
import com.instacart.formula.Inspector
import java.lang.AssertionError
import kotlin.reflect.KClass

class CountingInspector : Inspector {
    private var runCount = 0
    private val evaluatedList = mutableListOf<Class<*>>()
    private val actionsStarted = mutableListOf<Class<*>>()
    private val stateTransitions = mutableListOf<Class<*>>()

    override fun onEvaluateFinished(formulaType: KClass<*>, output: Any?, evaluated: Boolean) {
        if (evaluated) {
            evaluatedList.add(formulaType.java)
        }
    }

    override fun onRunStarted(evaluate: Boolean) {
        runCount += 1
    }

    override fun onActionStarted(formulaType: KClass<*>, action: DeferredAction<*>) {
        actionsStarted.add(formulaType.java)
    }

    override fun onStateChanged(formulaType: KClass<*>, event: Any?, old: Any?, new: Any?) {
        stateTransitions.add(formulaType.java)
    }

    fun assertEvaluationCount(expected: Int) = apply {
        val evaluationCount = evaluatedList.size
        if (evaluationCount != expected) {
            throw AssertionError("Evaluation count does not match - count: $evaluationCount, expected: $expected")
        }
    }

    fun assertEvaluationCount(formulaType: KClass<*>, expected: Int) = apply {
        val evaluationCount = evaluatedList.filter { it == formulaType.java }.size
        if (evaluationCount != expected) {
            throw AssertionError("Evaluation count does not match - count: $evaluationCount, expected: $expected, list: $evaluatedList")
        }
    }

    fun assertRunCount(expected: Int) = apply {
        if (runCount != expected) {
            throw AssertionError("Run count does not match - count: $runCount, expected: $expected")
        }
    }

    fun assertActionsStarted(expected: Int) = apply {
        val actionsStartedCount = actionsStarted.size
        if (actionsStartedCount != expected) {
            throw AssertionError("Actions started count does not match - count: $actionsStartedCount, expected: $expected")
        }
    }

    fun assertStateTransitions(formulaType: KClass<*>, expected: Int) = apply {
        val stateTransitionCount = stateTransitions.filter { it == formulaType.java }.size
        if (stateTransitionCount != expected) {
            throw AssertionError("State transition count does not match - count: $stateTransitionCount, expected: $expected, list: $stateTransitions")
        }
    }
}
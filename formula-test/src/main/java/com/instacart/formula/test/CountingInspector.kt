package com.instacart.formula.test

import com.instacart.formula.Inspector
import java.lang.AssertionError
import kotlin.reflect.KClass

class CountingInspector : Inspector {
    private var runCount = 0
    private val evaluatedList = mutableListOf<Class<*>>()

    override fun onEvaluateFinished(formulaType: KClass<*>, output: Any?, evaluated: Boolean) {
        if (evaluated) {
            evaluatedList.add(formulaType.java)
        }
    }

    override fun onRunStarted(evaluate: Boolean) {
        runCount += 1
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
}
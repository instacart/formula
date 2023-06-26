package com.instacart.formula.internal

import com.instacart.formula.DeferredAction
import com.instacart.formula.Inspector
import com.instacart.formula.Transition
import kotlin.reflect.KClass

class TestInspector : Inspector {
    val events = mutableListOf<String>()

    override fun onFormulaStarted(formulaType: KClass<*>) {
        events.add("formula-started: ${formulaType.qualifiedName}")
    }

    override fun onEvaluateStarted(formulaType: KClass<*>) {
        events.add("evaluate-started: ${formulaType.qualifiedName}")
    }

    override fun onEvaluateFinished(formulaType: KClass<*>) {
        events.add("evaluate-finished: ${formulaType.qualifiedName}")
    }

    override fun onExecutionStarted() {
        events.add("execution-started")
    }

    override fun onExecutionFinished() {
        events.add("execution-finished")
    }

    override fun onActionStarted(formulaType: KClass<*>, action: DeferredAction<*>) {
        events.add("action-started: ${formulaType.qualifiedName}")
    }

    override fun onActionFinished(formulaType: KClass<*>, action: DeferredAction<*>) {
        events.add("action-finished: ${formulaType.qualifiedName}")
    }

    override fun onTransition(
        formulaType: KClass<*>,
        result: Transition.Result<*>,
        requiresEvaluation: Boolean
    ) {
        events.add("transition: ${formulaType.qualifiedName}")
    }

    override fun onFormulaFinished(formulaType: KClass<*>) {
        events.add("formula-finished: ${formulaType.qualifiedName}")
    }
}
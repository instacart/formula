package com.instacart.formula.internal

import com.instacart.formula.DeferredAction
import com.instacart.formula.Inspector
import com.instacart.formula.Transition
import kotlin.reflect.KClass

class TestInspector : Inspector {
    val events = mutableListOf<String>()

    override fun onRunStarted(evaluate: Boolean) {
        events.add("formula-run-started")
    }

    override fun onRunFinished() {
        events.add("formula-run-finished")
    }

    override fun onFormulaStarted(formulaType: KClass<*>) {
        events.add("formula-started: ${formulaType.qualifiedName}")
    }

    override fun onEvaluateStarted(formulaType: KClass<*>, state: Any?) {
        events.add("evaluate-started: ${formulaType.qualifiedName}")
    }

    override fun onEvaluateFinished(formulaType: KClass<*>, output: Any?, evaluated: Boolean) {
        events.add("evaluate-finished: ${formulaType.qualifiedName}")
    }

    override fun onActionStarted(formulaType: KClass<*>, action: DeferredAction<*>) {
        events.add("action-started: ${formulaType.qualifiedName}")
    }

    override fun onActionFinished(formulaType: KClass<*>, action: DeferredAction<*>) {
        events.add("action-finished: ${formulaType.qualifiedName}")
    }

    override fun onStateChanged(formulaType: KClass<*>, event: Any?, old: Any?, new: Any?) {
        events.add("state-changed: ${formulaType.qualifiedName}")
    }

    override fun onFormulaFinished(formulaType: KClass<*>) {
        events.add("formula-finished: ${formulaType.qualifiedName}")
    }
}
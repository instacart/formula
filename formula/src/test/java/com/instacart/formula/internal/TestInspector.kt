package com.instacart.formula.internal

import com.instacart.formula.DeferredAction
import com.instacart.formula.plugin.Inspector
import kotlin.reflect.KClass

class TestInspector : Inspector {
    val events = mutableListOf<String>()

    override fun onRunStarted(evaluate: Boolean) {
        super.onRunStarted(evaluate)
        events.add("formula-run-started")
    }

    override fun onRunFinished() {
        super.onRunFinished()
        events.add("formula-run-finished")
    }

    override fun onFormulaStarted(formulaType: KClass<*>) {
        super.onFormulaStarted(formulaType)
        events.add("formula-started: ${formulaType.qualifiedName}")
    }

    override fun onEvaluateStarted(formulaType: KClass<*>, state: Any?) {
        super.onEvaluateStarted(formulaType, state)
        events.add("evaluate-started: ${formulaType.qualifiedName}")
    }

    override fun onEvaluateFinished(formulaType: KClass<*>, output: Any?, evaluated: Boolean) {
        super.onEvaluateFinished(formulaType, output, evaluated)
        events.add("evaluate-finished: ${formulaType.qualifiedName}")
    }

    override fun onActionStarted(formulaType: KClass<*>, action: DeferredAction<*>) {
        super.onActionStarted(formulaType, action)
        events.add("action-started: ${formulaType.qualifiedName}")
    }

    override fun onActionFinished(formulaType: KClass<*>, action: DeferredAction<*>) {
        super.onActionFinished(formulaType, action)
        events.add("action-finished: ${formulaType.qualifiedName}")
    }

    override fun onInputChanged(formulaType: KClass<*>, prevInput: Any?, newInput: Any?) {
        super.onInputChanged(formulaType, prevInput, newInput)
        events.add("input-changed: ${formulaType.qualifiedName}")
    }

    override fun onStateChanged(formulaType: KClass<*>, event: Any?, old: Any?, new: Any?) {
        super.onStateChanged(formulaType, event, old, new)
        events.add("state-changed: ${formulaType.qualifiedName}")
    }

    override fun onFormulaFinished(formulaType: KClass<*>) {
        super.onFormulaFinished(formulaType)
        events.add("formula-finished: ${formulaType.qualifiedName}")
    }

    override fun onBatchStarted(updateCount: Int) {
        super.onBatchStarted(updateCount)
        events.add("batch-started: $updateCount updates")
    }

    override fun onBatchFinished() {
        super.onBatchFinished()
        events.add("batch-finished")
    }
}
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

    override fun onFormulaStarted(formulaType: Class<*>) {
        super.onFormulaStarted(formulaType)
        events.add("formula-started: ${name(formulaType)}")
    }

    override fun onEvaluateStarted(formulaType: Class<*>, state: Any?) {
        super.onEvaluateStarted(formulaType, state)
        events.add("evaluate-started: ${name(formulaType)}")
    }

    override fun onEvaluateFinished(formulaType: Class<*>, output: Any?, evaluated: Boolean) {
        super.onEvaluateFinished(formulaType, output, evaluated)
        events.add("evaluate-finished: ${name(formulaType)}")
    }

    override fun onActionStarted(formulaType: Class<*>, action: DeferredAction<*>) {
        super.onActionStarted(formulaType, action)
        events.add("action-started: ${name(formulaType)}")
    }

    override fun onActionFinished(formulaType: Class<*>, action: DeferredAction<*>) {
        super.onActionFinished(formulaType, action)
        events.add("action-finished: ${name(formulaType)}")
    }

    override fun onInputChanged(formulaType: Class<*>, prevInput: Any?, newInput: Any?) {
        super.onInputChanged(formulaType, prevInput, newInput)
        events.add("input-changed: ${name(formulaType)}")
    }

    override fun onStateChanged(formulaType: Class<*>, event: Any?, old: Any?, new: Any?) {
        super.onStateChanged(formulaType, event, old, new)
        events.add("state-changed: ${name(formulaType)}")
    }

    override fun onFormulaFinished(formulaType: Class<*>) {
        super.onFormulaFinished(formulaType)
        events.add("formula-finished: ${name(formulaType)}")
    }

    override fun onBatchStarted(updateCount: Int) {
        super.onBatchStarted(updateCount)
        events.add("batch-started: $updateCount updates")
    }

    override fun onBatchFinished() {
        super.onBatchFinished()
        events.add("batch-finished")
    }

    private fun name(formula: Class<*>): String {
        val simpleName = formula.simpleName
        return if (simpleName.isNotEmpty()) {
            "${formula.packageName}.$simpleName"
        } else {
            "null"
        }
    }
}
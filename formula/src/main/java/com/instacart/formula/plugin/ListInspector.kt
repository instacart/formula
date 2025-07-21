package com.instacart.formula.plugin

import com.instacart.formula.DeferredAction

class ListInspector(
    private val inspectors: List<Inspector>,
) : Inspector {
    override fun onFormulaStarted(formulaType: Class<*>) {
        forEachInspector { onFormulaStarted(formulaType) }
    }

    override fun onFormulaFinished(formulaType: Class<*>) {
        forEachInspector { onFormulaFinished(formulaType) }
    }

    override fun onEvaluateStarted(formulaType: Class<*>, state: Any?) {
        forEachInspector { onEvaluateStarted(formulaType, state) }
    }

    override fun onInputChanged(formulaType: Class<*>, prevInput: Any?, newInput: Any?) {
        forEachInspector { onInputChanged(formulaType, prevInput, newInput) }
    }

    override fun onEvaluateFinished(formulaType: Class<*>, output: Any?, evaluated: Boolean) {
        forEachInspector { onEvaluateFinished(formulaType, output, evaluated) }
    }

    override fun onActionStarted(formulaType: Class<*>, action: DeferredAction<*>) {
        forEachInspector { onActionStarted(formulaType, action) }
    }

    override fun onActionFinished(formulaType: Class<*>, action: DeferredAction<*>) {
        forEachInspector { onActionFinished(formulaType, action) }
    }

    override fun onStateChanged(formulaType: Class<*>, event: Any?, old: Any?, new: Any?) {
        forEachInspector { onStateChanged(formulaType, event, old, new) }
    }

    override fun onRunStarted(evaluate: Boolean) {
        forEachInspector { onRunStarted(evaluate) }
    }

    override fun onRunFinished() {
        forEachInspector { onRunFinished() }
    }

    override fun onBatchStarted(updateCount: Int) {
        forEachInspector { onBatchStarted(updateCount) }
    }

    override fun onBatchFinished() {
        forEachInspector { onBatchFinished() }
    }

    private inline fun forEachInspector(callback: Inspector.() -> Unit) {
        for (inspector in inspectors) {
            inspector.callback()
        }
    }
}
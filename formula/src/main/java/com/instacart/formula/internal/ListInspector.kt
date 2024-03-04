package com.instacart.formula.internal

import com.instacart.formula.DeferredAction
import com.instacart.formula.plugin.Inspector
import kotlin.reflect.KClass

internal class ListInspector(
    private val inspectors: List<Inspector>,
) : Inspector {
    override fun onFormulaStarted(formulaType: KClass<*>) {
        forEachInspector { onFormulaStarted(formulaType) }
    }

    override fun onFormulaFinished(formulaType: KClass<*>) {
        forEachInspector { onFormulaFinished(formulaType) }
    }

    override fun onEvaluateStarted(formulaType: KClass<*>, state: Any?) {
        forEachInspector { onEvaluateStarted(formulaType, state) }
    }

    override fun onInputChanged(formulaType: KClass<*>, prevInput: Any?, newInput: Any?) {
        forEachInspector { onInputChanged(formulaType, prevInput, newInput) }
    }

    override fun onEvaluateFinished(formulaType: KClass<*>, output: Any?, evaluated: Boolean) {
        forEachInspector { onEvaluateFinished(formulaType, output, evaluated) }
    }

    override fun onActionStarted(formulaType: KClass<*>, action: DeferredAction<*>) {
        forEachInspector { onActionStarted(formulaType, action) }
    }

    override fun onActionFinished(formulaType: KClass<*>, action: DeferredAction<*>) {
        forEachInspector { onActionFinished(formulaType, action) }
    }

    override fun onStateChanged(formulaType: KClass<*>, event: Any?, old: Any?, new: Any?) {
        forEachInspector { onStateChanged(formulaType, event, old, new) }
    }

    override fun onRunStarted(evaluate: Boolean) {
        forEachInspector { onRunStarted(evaluate) }
    }

    override fun onRunFinished() {
        forEachInspector { onRunFinished() }
    }

    private inline fun forEachInspector(callback: Inspector.() -> Unit) {
        for (inspector in inspectors) {
            inspector.callback()
        }
    }
}
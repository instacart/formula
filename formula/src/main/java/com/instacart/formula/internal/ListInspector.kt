package com.instacart.formula.internal

import com.instacart.formula.DeferredAction
import com.instacart.formula.Inspector
import kotlin.reflect.KClass

internal class ListInspector(
    private val inspectors: List<Inspector>,
): Inspector {
    override fun onFormulaStarted(formulaType: KClass<*>) {
        for (inspector in inspectors) {
            inspector.onFormulaStarted(formulaType)
        }
    }

    override fun onFormulaFinished(formulaType: KClass<*>) {
        for (inspector in inspectors) {
            inspector.onFormulaFinished(formulaType)
        }
    }

    override fun onEvaluateStarted(formulaType: KClass<*>, state: Any?) {
        for (inspector in inspectors) {
            inspector.onEvaluateStarted(formulaType, state)
        }
    }

    override fun onInputChanged(formulaType: KClass<*>, prevInput: Any?, newInput: Any?) {
        for (inspector in inspectors) {
            inspector.onInputChanged(formulaType, prevInput, newInput)
        }
    }

    override fun onEvaluateFinished(formulaType: KClass<*>, output: Any?, evaluated: Boolean) {
        for (inspector in inspectors) {
            inspector.onEvaluateFinished(formulaType, output, evaluated)
        }
    }

    override fun onActionStarted(formulaType: KClass<*>, action: DeferredAction<*>) {
        for (inspector in inspectors) {
            inspector.onActionStarted(formulaType, action)
        }
    }

    override fun onActionFinished(formulaType: KClass<*>, action: DeferredAction<*>) {
        for (inspector in inspectors) {
            inspector.onActionFinished(formulaType, action)
        }
    }

    override fun onStateChanged(formulaType: KClass<*>, old: Any?, new: Any?) {
        for (inspector in inspectors) {
            inspector.onStateChanged(formulaType, old, new)
        }
    }

    override fun onRunStarted(evaluate: Boolean) {
        for (inspector in inspectors) {
            inspector.onRunStarted(evaluate)
        }
    }

    override fun onRunFinished() {
        for (inspector in inspectors) {
            inspector.onRunFinished()
        }
    }
}
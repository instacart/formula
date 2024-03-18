package com.instacart.formula.plugin

import com.instacart.formula.DeferredAction
import kotlin.reflect.KClass

/**
 * Inspector allows to track what [Formula] is doing. It's useful for debugging various issues.
 */
interface Inspector {

    /**
     * Called when Formula or child formula is started.
     *
     * @param formulaType Formula type used to filter for specific events.
     */
    fun onFormulaStarted(formulaType: KClass<*>) = Unit

    /**
     * Called when Formula or child formula is finished.
     *
     * @param formulaType Formula type used to filter for specific events.
     */
    fun onFormulaFinished(formulaType: KClass<*>) = Unit

    /**
     * Called when Formula evaluation is started.
     *
     * @param formulaType Formula type used to filter for specific events.
     */
    fun onEvaluateStarted(formulaType: KClass<*>, state: Any?) = Unit

    /**
     * Called when Formula input has changed.
     *
     * @param formulaType Formula type used to filter for specific events.
     * @param prevInput Previous input used
     * @param newInput New input value
     */
    fun onInputChanged(formulaType: KClass<*>, prevInput: Any?, newInput: Any?) = Unit

    /**
     * Called when Formula evaluation is finished.
     *
     * @param formulaType Formula type used to filter for specific events.
     * @param evaluated Indicates if evaluate had to run. If false, this means that we re-used previous output.
     */
    fun onEvaluateFinished(formulaType: KClass<*>, output: Any?, evaluated: Boolean) = Unit

    /**
     * Called when an action is started.
     *
     * @param formulaType Formula type used to filter for specific events.
     * @param action Action that was started.
     */
    fun onActionStarted(formulaType: KClass<*>, action: DeferredAction<*>) = Unit

    /**
     * Called when an action is finished.
     *
     * @param formulaType Formula type used to filter for specific events.
     * @param action Action that was finished.
     */
    fun onActionFinished(formulaType: KClass<*>, action: DeferredAction<*>) = Unit

    /**
     * Called when a state change happens
     *
     * @param formulaType Formula type used to filter for specific events.
     * @param event Event that triggered this state change
     * @param old Previous state value
     * @param new New state value
     */
    fun onStateChanged(formulaType: KClass<*>, event: Any?, old: Any?, new: Any?) = Unit

    /**
     * Called when [FormulaRuntime.run] is called. This method in combination with [onRunFinished]
     * is a good way to measure overall formula performance.
     */
    fun onRunStarted(evaluate: Boolean) = Unit

    /**
     * Called when [FormulaRuntime.run] has finished evaluating and
     * executing actions.
     */
    fun onRunFinished() = Unit

    /**
     * Called when batch execution started.
     */
    fun onBatchStarted(updateCount: Int) = Unit

    /**
     * Called when batch execution finished
     */
    fun onBatchFinished() = Unit
}
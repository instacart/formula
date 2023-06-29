package com.instacart.formula

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
     * Execution phase performs various side-effects such as:
     * - starting an action
     * - stopping an action
     * - terminating detached child formulas
     * - executing transition side-effects
     *
     * This function is called when we start the execution phase.
     */
    fun onExecutionStarted() = Unit

    /**
     * Execution phase performs various side-effects such as:
     * - starting an action
     * - stopping an action
     * - terminating detached child formulas
     * - executing transition side-effects
     *
     * This function is called when execution phase finished.
     */
    fun onExecutionFinished() = Unit

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
     * Called when a transition happens
     *
     * @param formulaType Formula type used to filter for specific events.
     * @param result Transition result used to check what kind of transition.
     * @param evaluate Indicates if transition requires a new evaluation.
     */
    fun onTransition(formulaType: KClass<*>, result: Transition.Result<*>, evaluate: Boolean) = Unit

}
package com.instacart.formula

import com.instacart.formula.plugin.Inspector
import com.instacart.formula.plugin.Dispatcher
import com.instacart.formula.plugin.FormulaError

/**
 * @param defaultDispatcher Dispatcher used for event processing (this can be overwritten by
 * individual events). By default, formula runs on the thread on which the event arrived on.
 * @param inspector Inspector that will be used when configuring the formula.
 * @param isValidationEnabled A boolean that validates inputs and outputs by
 * running [Formula.evaluate] twice. Should NOT be used in production builds,
 * preferably only unit tests.
 */
class RuntimeConfig(
    val defaultDispatcher: Dispatcher? = null,
    val inspector: Inspector? = null,
    val isValidationEnabled: Boolean = false,
    val onError: ((FormulaError) -> Unit)? = null,
)

package com.instacart.formula

import com.instacart.formula.plugin.Inspector
import com.instacart.formula.plugin.Dispatcher

/**
 * @param defaultDispatcher Dispatcher used for event processing (this can be overwritten by
 * individual events). By default, formula runs on the thread on which the event arrived on.
 * @param inspector Inspector that will be used when configuring the formula.
 */
class RuntimeConfig(
    val defaultDispatcher: Dispatcher? = null,
    val inspector: Inspector? = null,
)

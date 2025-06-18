package com.instacart.formula.internal

import com.instacart.formula.plugin.Dispatcher
import com.instacart.formula.plugin.FormulaError
import com.instacart.formula.plugin.Plugin

class TestPlugin(
    private val mainDispatcher: Dispatcher? = null,
    private val backgroundDispatcher: Dispatcher? = null,
    private val defaultDispatcher: Dispatcher? = null,
) : Plugin {
    val errors = mutableListOf<FormulaError>()

    override fun onError(error: FormulaError) {
        errors += error
    }

    override fun mainThreadDispatcher(): Dispatcher? {
        return mainDispatcher ?: super.mainThreadDispatcher()
    }

    override fun backgroundThreadDispatcher(): Dispatcher? {
        return backgroundDispatcher ?: super.backgroundThreadDispatcher()
    }

    override fun defaultDispatcher(): Dispatcher? {
        return defaultDispatcher ?: super.defaultDispatcher()
    }
}
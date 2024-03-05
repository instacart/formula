package com.instacart.formula.subjects

import com.instacart.formula.plugin.Plugin
import com.instacart.formula.plugin.Dispatcher

class TestDispatcherPlugin(val defaultDispatcher: Dispatcher? = null) : Plugin {
    val mainDispatcher = IncrementingDispatcher()
    val backgroundDispatcher = IncrementingDispatcher()

    override fun mainThreadDispatcher(): Dispatcher {
        return mainDispatcher
    }

    override fun backgroundThreadDispatcher(): Dispatcher {
        return backgroundDispatcher
    }

    override fun defaultDispatcher(): Dispatcher? {
        return defaultDispatcher
    }
}
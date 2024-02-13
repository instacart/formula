package com.instacart.formula.subjects

import com.instacart.formula.Plugin
import com.instacart.formula.plugin.Dispatcher

class TestDispatcherPlugin : Plugin {
    val mainDispatcher = IncrementingDispatcher()
    val backgroundDispatcher = IncrementingDispatcher()

    override fun mainThreadDispatcher(): Dispatcher {
        return mainDispatcher
    }

    override fun backgroundThreadDispatcher(): Dispatcher {
        return backgroundDispatcher
    }
}
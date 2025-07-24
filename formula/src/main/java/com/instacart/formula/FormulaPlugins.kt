package com.instacart.formula

import com.instacart.formula.plugin.ListInspector
import com.instacart.formula.plugin.Dispatcher
import com.instacart.formula.plugin.FormulaError
import com.instacart.formula.plugin.Inspector
import com.instacart.formula.plugin.Plugin
import kotlin.reflect.KClass

object FormulaPlugins {
    @Volatile private var plugin: Plugin? = null

    fun setPlugin(plugin: Plugin?) {
        this.plugin = plugin
    }

    fun inspector(type: Class<*>, local: Inspector?): Inspector? {
        val global = plugin?.inspector(type)
        return when {
            global == null -> local
            local == null -> global
            else -> ListInspector(listOf(global, local))
        }
    }

    /**
     * Notified when there is an error in the formula.
     */
    fun onError(error: FormulaError) {
        plugin?.onError(error)
    }

    fun mainThreadDispatcher(): Dispatcher {
        return plugin?.mainThreadDispatcher() ?: Dispatcher.None
    }

    fun backgroundThreadDispatcher(): Dispatcher {
        return plugin?.backgroundThreadDispatcher() ?: Dispatcher.None
    }

    fun defaultDispatcher(): Dispatcher {
        return plugin?.defaultDispatcher() ?: Dispatcher.None
    }
}
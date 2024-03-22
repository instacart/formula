package com.instacart.formula

import com.instacart.formula.internal.ListInspector
import com.instacart.formula.plugin.Dispatcher
import com.instacart.formula.plugin.Inspector
import com.instacart.formula.plugin.Plugin
import kotlin.reflect.KClass

object FormulaPlugins {
    @Volatile private var plugin: Plugin? = null

    fun setPlugin(plugin: Plugin?) {
        this.plugin = plugin
    }

    fun inspector(type: KClass<*>, local: Inspector?): Inspector? {
        val global = plugin?.inspector(type)
        return when {
            global == null -> local
            local == null -> global
            else -> ListInspector(listOf(global, local))
        }
    }

    fun onDuplicateChildKey(
        parentFormulaType: Class<*>,
        childFormulaType: Class<*>,
        key: Any,
    ) {
       plugin?.onDuplicateChildKey(parentFormulaType, childFormulaType, key)
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
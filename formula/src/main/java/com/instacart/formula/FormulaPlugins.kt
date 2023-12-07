package com.instacart.formula

import com.instacart.formula.internal.ListInspector
import kotlin.reflect.KClass

object FormulaPlugins {
    private var plugin: Plugin? = null

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
}
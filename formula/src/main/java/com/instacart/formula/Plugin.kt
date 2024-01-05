package com.instacart.formula

import com.instacart.formula.plugin.Dispatcher
import kotlin.reflect.KClass

interface Plugin {

    /**
     * Provide a global main thread dispatcher.
     */
    fun mainThreadDispatcher(): Dispatcher? {
        return null
    }

    /**
     * A global callback to create [Inspector] for any formula. This will be called once when
     * formula is initially started.
     *
     * @param type Formula type.
     */
    fun inspector(type: KClass<*>): Inspector? {
        return null
    }

    /**
     * Notified when there is a duplicate child key detected.
     */
    fun onDuplicateChildKey(
        parentType: Class<*>,
        childFormulaType: Class<*>,
        key: Any,
    ) = Unit
}
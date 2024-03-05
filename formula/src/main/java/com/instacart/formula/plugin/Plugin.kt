package com.instacart.formula.plugin

import kotlin.reflect.KClass

interface Plugin {
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

    /**
     * Dispatcher for the main thread. It will be used to execute
     * effects defined by [Effect.Main].
     */
    fun mainThreadDispatcher(): Dispatcher? {
        return null
    }

    /**
     * Dispatcher for the background thread. It will be used to execute
     * effects defined by [Effect.Background].
     */
    fun backgroundThreadDispatcher(): Dispatcher? {
        return null
    }

    /**
     * Default dispatcher that each formula runtime will use to process events. This
     * can be overwritten by each formula individually.
     */
    fun defaultDispatcher(): Dispatcher? {
        return null
    }
}
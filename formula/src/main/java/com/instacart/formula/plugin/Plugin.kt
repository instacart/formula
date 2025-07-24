package com.instacart.formula.plugin


interface Plugin {
    /**
     * A global callback to create [Inspector] for any formula. This will be called once when
     * formula is initially started.
     *
     * @param type Formula type.
     */
    fun inspector(type: Class<*>): Inspector? {
        return null
    }

    /**
     * Notified when an error is thrown in a formula.
     */
    fun onError(error: FormulaError) = Unit

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
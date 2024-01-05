package com.instacart.formula.plugin

/**
 * Dispatcher ensures that we are executing certain functions on the appropriate
 * thread.
 */
/**
 * To ensure that formula outputs and side-effects are executed correctly,
 * we
 * To ensure that formula outputs are emitted on the correct thread,
 * we are adding a
 * To ensure that formula executes correctly,
 * To ensure that events are handled cor
 */
interface Dispatcher {

    /**
     * Checks if the current thread doesn't match and the main thread and we
     * need to change threads.
     */
    fun isDispatchNeeded(): Boolean

    /**
     * Executes [runnable] on the main thread.
     */
    fun dispatch(runnable: () -> Unit)
}
package com.instacart.formula.plugin

import com.instacart.formula.FormulaPlugins

/**
 * Dispatches executables to a specific thread.
 */
interface Dispatcher {
    object None : Dispatcher {
        override fun dispatch(executable: () -> Unit) {
            executable()
        }

        override fun isDispatchNeeded(): Boolean {
            return false
        }
    }

    /**
     * Uses [Plugin.mainThreadDispatcher] to dispatch executables.
     */
    object Main : Dispatcher {
        override fun dispatch(executable: () -> Unit) {
            val delegate = FormulaPlugins.mainThreadDispatcher()
            delegate.dispatch(executable)
        }

        override fun isDispatchNeeded(): Boolean {
            val delegate = FormulaPlugins.mainThreadDispatcher()
            return delegate.isDispatchNeeded()
        }
    }

    /**
     * Uses [Plugin.backgroundThreadDispatcher] to dispatch executables.
     */
    object Background : Dispatcher {
        override fun dispatch(executable: () -> Unit) {
            val delegate = FormulaPlugins.backgroundThreadDispatcher()
            delegate.dispatch(executable)
        }

        override fun isDispatchNeeded(): Boolean {
            val delegate = FormulaPlugins.backgroundThreadDispatcher()
            return delegate.isDispatchNeeded()
        }
    }

    /**
     * Dispatches [executable] to a thread specified by the [Dispatcher].
     */
    fun dispatch(executable: () -> Unit)

    /**
     * Returns true if dispatching event is needed
     */
    fun isDispatchNeeded(): Boolean
}
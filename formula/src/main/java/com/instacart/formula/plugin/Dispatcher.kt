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
    }

    /**
     * Uses [Plugin.mainThreadDispatcher] to dispatch executables.
     */
    object Main : Dispatcher {
        override fun dispatch(executable: () -> Unit) {
            val delegate = FormulaPlugins.mainThreadDispatcher()
            delegate.dispatch(executable)
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
    }

    /**
     * Dispatches [executable] to a thread specified by the [Dispatcher].
     */
    fun dispatch(executable: () -> Unit)
}
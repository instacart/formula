package com.instacart.formula.plugin

/**
 * Dispatches executables to a specific thread.
 */
interface Dispatcher {
    object NoOp : Dispatcher {
        override fun dispatch(executable: () -> Unit) {
            executable()
        }
    }

    fun dispatch(executable: () -> Unit)
}
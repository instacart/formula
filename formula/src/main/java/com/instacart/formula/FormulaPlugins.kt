package com.instacart.formula

import java.util.concurrent.Executor

object FormulaPlugins {

    private var mainThreadExecutor: Executor? = null

    fun setMainThreadExecutor(executor: Executor) {
        mainThreadExecutor = executor
    }

    fun <T> callOnMain(callback: (T) -> Unit, value: T) {
        val executor = mainThreadExecutor
        if (executor != null) {
            executor.execute { callback(value) }
        } else {
            callback(value)
        }
    }
}
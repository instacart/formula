package com.instacart.formula.rxjava3

interface RxJavaRuntimeErrorHandler {
    /**
     * @param error [Throwable] that occurred
     *
     * @return true if error was handled, false otherwise
     */
    fun onError(error: Throwable): Boolean
}

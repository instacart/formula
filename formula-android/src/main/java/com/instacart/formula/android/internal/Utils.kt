package com.instacart.formula.android.internal

import android.os.Handler
import android.os.Looper

internal object Utils {
    internal val mainThreadHandler = Handler(Looper.getMainLooper())

    fun assertMainThread() {
        if (!isMainThread()) {
            throw IllegalStateException("should be called on main thread: ${Thread.currentThread()}")
        }
    }

    fun isMainThread(): Boolean {
        return Looper.getMainLooper() == Looper.myLooper()
    }
}
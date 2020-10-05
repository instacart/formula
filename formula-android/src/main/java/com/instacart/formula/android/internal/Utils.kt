package com.instacart.formula.android.internal

import android.os.Looper

internal object Utils {
    fun assertMainThread() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw IllegalStateException("should be called on main thread: ${Thread.currentThread()}")
        }
    }
}
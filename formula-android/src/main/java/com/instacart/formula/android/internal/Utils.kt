package com.instacart.formula.android.internal

import android.os.Handler
import android.os.Looper

internal object Utils {
    internal val mainThreadHandler = Handler(Looper.getMainLooper())

    fun isMainThread(): Boolean {
        return Looper.getMainLooper() == Looper.myLooper()
    }
}
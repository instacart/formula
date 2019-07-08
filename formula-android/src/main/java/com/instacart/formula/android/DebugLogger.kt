package com.instacart.formula.android

import android.util.Log
import com.instacart.formula.Logger

class DebugLogger : Logger {

    override fun logEvent(event: String) {
        Log.d("formula", event)
    }
}
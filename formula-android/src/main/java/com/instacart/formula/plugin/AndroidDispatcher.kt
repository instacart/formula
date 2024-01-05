package com.instacart.formula.plugin

import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.core.os.MessageCompat

/**
 * Provides android implementation of main thread dispatcher.
 */
class AndroidDispatcher : Dispatcher {

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun isDispatchNeeded(): Boolean {
        return Looper.getMainLooper() == Looper.myLooper()
    }

    override fun dispatch(runnable: () -> Unit) {
        if (isDispatchNeeded()) {
            val message = Message.obtain(mainHandler, runnable)
            MessageCompat.setAsynchronous(message, true)
            mainHandler.sendMessage(message)
        } else {
            runnable()
        }
    }
}
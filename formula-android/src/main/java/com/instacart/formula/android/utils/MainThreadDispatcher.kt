package com.instacart.formula.android.utils

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.instacart.formula.plugin.Dispatcher

/**
 * Android main thread formula dispatcher.
 */
class MainThreadDispatcher : Dispatcher {
    private val handler = Handler(Looper.getMainLooper())

    override fun dispatch(executable: () -> Unit) {
        if (isDispatchNeeded()) {
            val message = Message.obtain(handler, executable)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                message.isAsynchronous = true
            }
            handler.sendMessage(message)
        } else {
            executable()
        }
    }

    override fun isDispatchNeeded(): Boolean {
        return Looper.getMainLooper().thread != Thread.currentThread()
    }
}

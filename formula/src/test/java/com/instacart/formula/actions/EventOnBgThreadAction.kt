package com.instacart.formula.actions

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import com.instacart.formula.test.TestEventCallback
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class EventOnBgThreadAction : Action<Int> {
    val errors = TestEventCallback<Throwable>()
    val latch = CountDownLatch(1)

    override fun key(): Any? = null

    override fun start(send: (Int) -> Unit): Cancelable? {
        Executors.newSingleThreadExecutor().execute {
            try {
                send(0)
            } catch (e: Throwable) {
                errors.invoke(e)
            } finally {
                latch.countDown()
            }
        }
        return null
    }
}
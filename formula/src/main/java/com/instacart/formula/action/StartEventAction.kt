package com.instacart.formula.action

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import kotlinx.coroutines.CoroutineScope

/**
 * Emits an event as soon as [Action] is initialized.
 */
internal class StartEventAction<Data>(
    private val data: Data
) : Action<Data> {

    override fun start(scope: CoroutineScope, send: (Data) -> Unit): Cancelable? {
        send(data)
        return null
    }

    override fun key(): Any? = data
}

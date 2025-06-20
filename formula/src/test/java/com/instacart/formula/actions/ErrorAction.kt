package com.instacart.formula.actions

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import kotlinx.coroutines.CoroutineScope

class ErrorAction(
    private val error: Throwable = RuntimeException("my error")
) : Action<Unit> {
    override fun start(scope: CoroutineScope, emitter: Action.Emitter<Unit>): Cancelable? {
        emitter.onError(error)
        return null
    }

    override fun key(): Any? = null
}
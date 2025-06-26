package com.instacart.formula.test

import com.instacart.formula.Action
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

class FlowRelay {
    private val sharedFlow = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    fun action(): Action<Unit> = Action.fromFlow { sharedFlow }

    fun triggerEvent() {
        sharedFlow.tryEmit(Unit)
    }
}
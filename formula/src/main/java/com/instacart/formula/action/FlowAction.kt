package com.instacart.formula.action

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A Formula [Action] that is created from a [Flow].
 */
@PublishedApi
internal class FlowAction<Event>(
    private val dispatcher: CoroutineDispatcher,
    private val key: Any?,
    private val factory: suspend () -> Flow<Event>
) : Action<Event> {

    override fun key(): Any? = key

    override fun start(scope: CoroutineScope, send: (Event) -> Unit): Cancelable {
        val job = scope.launch(
            context = dispatcher,
            start = CoroutineStart.UNDISPATCHED,
        ) {
            factory().collect {
                send(it)
            }
        }
        return Cancelable(job::cancel)
    }
}

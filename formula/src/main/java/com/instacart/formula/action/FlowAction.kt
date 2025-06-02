package com.instacart.formula.action

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * A Formula [Action] that is created from a [Flow].
 */
@PublishedApi
internal class FlowAction<Event>(
    private val context: CoroutineContext,
    private val key: Any?,
    private val factory: suspend () -> Flow<Event>
) : Action<Event> {

    override fun key(): Any? = key

    override fun start(scope: CoroutineScope, send: (Event) -> Unit): Cancelable {
        val job = scope.launch(start = CoroutineStart.UNDISPATCHED) {
            withContext(context) {
                factory().collect {
                    send(it)
                }
            }
        }
        return Cancelable(job::cancel)
    }
}

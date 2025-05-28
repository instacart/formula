package com.instacart.formula.action

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

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

    @OptIn(DelicateCoroutinesApi::class)
    override fun start(send: (Event) -> Unit): Cancelable {
        val job = GlobalScope.launch(
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

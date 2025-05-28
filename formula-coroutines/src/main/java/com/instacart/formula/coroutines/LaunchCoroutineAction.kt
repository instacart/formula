package com.instacart.formula.coroutines

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Adapter which allows to run a suspend function as an [Action].
 */
class LaunchCoroutineAction<Result> @PublishedApi internal constructor(
    private val dispatcher: CoroutineDispatcher,
    private val key: Any?,
    private val block: suspend () -> Result,
) : Action<Result> {

    override fun key(): Any? = key

    @OptIn(DelicateCoroutinesApi::class)
    override fun start(
        send: (Result) -> Unit,
    ): Cancelable {
        val job = GlobalScope.launch(
            context = dispatcher,
            start = CoroutineStart.UNDISPATCHED,
        ) {
            val result = block()
            send(result)
        }
        return Cancelable(job::cancel)
    }
}
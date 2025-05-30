package com.instacart.formula.action

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Adapter which allows to run a suspend function as an [Action].
 */
@PublishedApi
internal class LaunchCoroutineAction<Result>(
    private val dispatcher: CoroutineDispatcher,
    private val key: Any?,
    private val block: suspend () -> Result,
) : Action<Result> {

    override fun key(): Any? = key

    override fun start(
        scope: CoroutineScope,
        send: (Result) -> Unit,
    ): Cancelable {
        val job = scope.launch(
            context = dispatcher,
            start = CoroutineStart.UNDISPATCHED,
        ) {
            val result = block()
            send(result)
        }
        return Cancelable(job::cancel)
    }
}

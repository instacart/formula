package com.instacart.formula.action

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Adapter which allows to run a suspend function as an [Action].
 */
@PublishedApi
internal class LaunchCoroutineAction<Result>(
    private val context: CoroutineContext,
    private val key: Any?,
    private val block: suspend () -> Result,
) : Action<Result> {

    override fun key(): Any? = key

    override fun start(
        scope: CoroutineScope,
        send: (Result) -> Unit,
    ): Cancelable {
        val job = scope.launch(start = CoroutineStart.UNDISPATCHED) {
            withContext(context) {
                val result = block()
                send(result)
            }
        }
        return Cancelable(job::cancel)
    }
}

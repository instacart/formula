package com.instacart.formula.action

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import kotlinx.coroutines.CancellationException
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

    override fun start(scope: CoroutineScope, emitter: Action.Emitter<Result>): Cancelable {
        val job = scope.launch(start = CoroutineStart.UNDISPATCHED) {
            withContext(context) {
                try {
                    val result = block()
                    emitter.onEvent(result)
                } catch (e: Exception) {
                    if (e !is CancellationException) {
                        emitter.onError(e)
                    }
                }
            }
        }
        return Cancelable(job::cancel)
    }
}

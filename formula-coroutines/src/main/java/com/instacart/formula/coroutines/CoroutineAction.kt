package com.instacart.formula.coroutines

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *
 * Adapter which allows to run a coroutine as an [Action].
 */
class CoroutineAction<Result> @PublishedApi internal constructor(
    private val dispatcher: CoroutineDispatcher,
    private val key: Any?,
    private val block: suspend () -> Result,
) : Action<Result> {
    companion object {
        /**
         * Creates an [Action] that runs a [block] suspend function.
         *
         * Note: by default, the coroutine uses [Dispatchers.Unconfined]
         */
        fun <Result> launch(
            coroutineDispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
            block: suspend () -> Result,
        ): Action<Result> {
            return launch(
                coroutineDispatcher = coroutineDispatcher,
                key = null,
                block = block,
            )
        }

        /**
         * Creates an [Action] that runs a [block] suspend function.
         *
         * Note: by default, the coroutine uses [Dispatchers.Unconfined]
         */
        fun <Result> launch(
            key: Any?,
            coroutineDispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
            block: suspend () -> Result,
        ): Action<Result> {
            return CoroutineAction(
                dispatcher = coroutineDispatcher,
                key = key,
                block = block,
            )
        }

        /**
         * Creates an [Action] that runs a [block] suspend function and catches any exceptions.
         *
         * Note: by default, the coroutine uses [Dispatchers.Unconfined]
         */
        inline fun <Result> launchCatching(
            coroutineDispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
            crossinline block: suspend () -> Result,
        ): Action<kotlin.Result<Result>> {
            return launchCatching(
                key = null,
                coroutineDispatcher =  coroutineDispatcher,
                block = block,
            )
        }

        /**
         * Creates an [Action] that runs a [block] suspend function and catches any exceptions.
         *
         * Note: by default, the coroutine uses [Dispatchers.Unconfined]
         */
        inline fun <Result> launchCatching(
            key: Any?,
            coroutineDispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
            crossinline block: suspend () -> Result,
        ): Action<kotlin.Result<Result>> {
            return CoroutineAction(
                dispatcher =  coroutineDispatcher,
                key = key,
                block = {
                    runCatchingCoroutines(block)
                },
            )
        }
    }

    override fun key(): Any? = key

    @OptIn(DelicateCoroutinesApi::class)
    override fun start(
        send: (Result) -> Unit,
    ): Cancelable {
        val job = GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
            withContext(dispatcher) {
                val result = block()
                send(result)
            }
        }
        return Cancelable(job::cancel)
    }
}
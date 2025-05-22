package com.instacart.formula.action

import com.instacart.formula.Action
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

/**
 * A factory to create actions from coroutine suspend functions or flows.
 */
object CoroutineAction {
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
        return LaunchCoroutineAction(
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
            coroutineDispatcher = coroutineDispatcher,
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
        return LaunchCoroutineAction(
            dispatcher = coroutineDispatcher,
            key = key,
            block = {
                runCatchingCoroutines(block)
            },
        )
    }

    /**
     * Creates an [Action] which will launch a [Flow] created by factory function [create].
     *
     * @param coroutineDispatcher A [CoroutineDispatcher] used to start the flow. By default, [Dispatchers.Unconfined] is used.
     */
    fun <Event> fromFlow(
        coroutineDispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
        create: suspend () -> Flow<Event>
    ): Action<Event> {
        return FlowAction(coroutineDispatcher, null, create)
    }

    /**
     * Creates an [Action] which will launch a [Flow] created by factory function [create].
     *
     * @param key Used to distinguish this [Action] from other actions.
     * @param coroutineDispatcher A [CoroutineDispatcher] used to start the flow. By default, [Dispatchers.Unconfined] is used.
     */
    fun <Event> fromFlow(
        key: Any?,
        coroutineDispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
        create: suspend () -> Flow<Event>
    ): Action<Event> {
        return FlowAction(coroutineDispatcher, key, create)
    }
}
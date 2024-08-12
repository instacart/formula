package com.instacart.formula.coroutines

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Adapter which allows creating Formula [Action] from Kotlin coroutine's. Take a
 * look at [FlowAction.fromFlow] to create an [Action] from [Flow] type.
 */
interface FlowAction<Event> : Action<Event> {

    companion object {
        /**
         * Creates an [Action] which will launch a [Flow] created by factory function [create].
         *
         * ```
         * FlowAction.fromFlow { locationManager.updates() }.onEvent { event ->
         *   transition()
         * }
         * ```
         */
        fun <Event> fromFlow(
            create: () -> Flow<Event>
        ): Action<Event> {
            return FlowActionImpl(null, create)
        }

        /**
         * Creates an [Action] which will launch a [Flow] created by factory function [create].
         *
         * ```
         * FlowAction.fromFlow(itemId) { repo.fetchItem(itemId) }.onEvent { event ->
         *   transition()
         * }
         * ```
         *
         * @param key Used to distinguish this [Action] from other actions.
         */
        fun <Event> fromFlow(
            key: Any?,
            create: () -> Flow<Event>
        ): Action<Event> {
            return FlowActionImpl(key, create)
        }
    }

    fun flow(): Flow<Event>

    @OptIn(DelicateCoroutinesApi::class)
    override fun start(send: (Event) -> Unit): Cancelable? {
        val job = flow()
            .onEach { send(it) }
            .launchIn(GlobalScope)
        return Cancelable(job::cancel)
    }
}

private data class FlowActionImpl<Event>(
    private val key: Any?,
    private val factory: () -> Flow<Event>
) : FlowAction<Event> {
    override fun flow(): Flow<Event> = factory()

    override fun key(): Any? = key
}
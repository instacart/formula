package com.instacart.formula.coroutines

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Adapter which allows creating Formula [Action] from a Kotlin suspend function. Take a
 * look at [SuspendAction.from] to create an [Action] from [suspend] type.
 */
interface SuspendAction<Event> : Action<Event> {

    companion object {
        /**
         * Creates an [Action] which will launch a [suspend] created by factory function [create]
         *
         * ```
         * SuspendAction.from { locationManager.currentLocation() }.onEvent { event ->
         *   transition()
         * }
         * ```
         */
        fun <Event> from(
            create: suspend () -> Event
        ): SuspendAction<Event> {
            return SuspendActionImpl(null, create)
        }

        /**
         * Creates an [Action] which will launch a [suspend] created by factory function [create].
         *
         * ```
         * SuspendAction.from(itemId) { repo.fetchItem(itemId) }.onEvent { event ->
         *   transition()
         * }
         * ```
         *
         * @param key Used to distinguish this [Action] from other actions.
         */
        fun <Event> from(
            key: Any?,
            create: suspend () -> Event
        ): SuspendAction<Event> {
            return SuspendActionImpl(key, create)
        }
    }

    suspend fun execute(): Event

    @OptIn(DelicateCoroutinesApi::class)
    override fun start(send: (Event) -> Unit): Cancelable? {
        val job = GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
            withContext(Dispatchers.Unconfined) {
                val event = execute()
                send(event)
            }
        }
        return Cancelable(job::cancel)
    }
}

private data class SuspendActionImpl<Event>(
    private val key: Any?,
    private val factory: suspend () -> Event
) : SuspendAction<Event> {

    override suspend fun execute(): Event = factory()

    override fun key(): Any? = key
}
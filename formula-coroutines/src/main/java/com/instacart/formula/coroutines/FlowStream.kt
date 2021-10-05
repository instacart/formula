package com.instacart.formula.coroutines

import com.instacart.formula.Cancelable
import com.instacart.formula.Stream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Formula [Stream] adapter to enable coroutine's Flow use.
 */
interface FlowStream<Event> : Stream<Event> {

    companion object {
        /**
         * Creates a [Stream] from a [Flow] factory [create].
         *
         * ```
         * events(FlowStream.fromFlow { locationManager.updates() }) { event ->
         *   transition()
         * }
         * ```
         */
        inline fun <Event> fromFlow(
            scope: CoroutineScope = MainScope(),
            crossinline create: () -> Flow<Event>
        ): Stream<Event> {
            return object : FlowStream<Event> {

                override val scope: CoroutineScope = scope

                override fun flow(): Flow<Event> {
                    return create()
                }

                override fun key(): Any = Unit
            }
        }

        /**
         * Creates a [Stream] from a [Flow] factory [create].
         *
         * ```
         * events(FlowStream.fromFlow(itemId) { repo.fetchItem(itemId) }) { event ->
         *   transition()
         * }
         * ```
         *
         * @param key Used to distinguish this [Stream] from other streams.
         */
        inline fun <Event> fromFlow(
            scope: CoroutineScope = MainScope(),
            key: Any?,
            crossinline create: () -> Flow<Event>
        ): Stream<Event> {
            return object : FlowStream<Event> {
                override val scope: CoroutineScope = scope

                override fun flow(): Flow<Event> {
                    return create()
                }

                override fun key(): Any? = key
            }
        }
    }

    fun flow(): Flow<Event>

    val scope: CoroutineScope

    override fun start(send: (Event) -> Unit): Cancelable? {
        val job = flow()
            .onEach { send(it) }
            .launchIn(scope)
        return Cancelable(job::cancel)
    }
}
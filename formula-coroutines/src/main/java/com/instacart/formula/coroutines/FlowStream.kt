package com.instacart.formula.coroutines

import com.instacart.formula.Cancelable
import com.instacart.formula.Stream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.runBlocking

/**
 * Formula [Stream] adapter to enable coroutine's Flow use.
 */
interface FlowStream<Message> : Stream<Message> {

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
        inline fun <Message> fromFlow(
            scope: CoroutineScope,
            crossinline create: () -> Flow<Message>
        ): Stream<Message> {
            return object : FlowStream<Message> {

                override val scope: CoroutineScope = scope

                override fun flow(): Flow<Message> {
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
        inline fun <Message> fromFlow(
            scope: CoroutineScope,
            key: Any?,
            crossinline create: () -> Flow<Message>
        ): Stream<Message> {
            return object : FlowStream<Message> {
                override val scope: CoroutineScope = scope

                override fun flow(): Flow<Message> {
                    return create()
                }

                override fun key(): Any? = key
            }
        }
    }

    fun flow(): Flow<Message>

    val scope: CoroutineScope

    override fun start(send: (Message) -> Unit): Cancelable? {
        val job = flow().launchIn(scope)
        return Cancelable(job::cancel)
    }
}
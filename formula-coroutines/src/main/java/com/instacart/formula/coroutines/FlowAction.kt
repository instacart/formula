package com.instacart.formula.coroutines

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
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
        inline fun <Event> fromFlow(
            crossinline create: () -> Flow<Event>
        ): Action<Event> {
            return object : FlowAction<Event> {

                override fun flow(): Flow<Event> {
                    return create()
                }

                override fun key(): Any = Unit
            }
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
        inline fun <Event> fromFlow(
            key: Any?,
            crossinline create: () -> Flow<Event>
        ): Action<Event> {
            return object : FlowAction<Event> {

                override fun flow(): Flow<Event> {
                    return create()
                }

                override fun key(): Any? = key
            }
        }
    }

    fun flow(): Flow<Event>

    override fun start(send: (Event) -> Unit): Cancelable? {
        val scope = MainScope()
        flow().onEach { send(it) }.launchIn(scope)
        return Cancelable(scope::cancel)
    }
}
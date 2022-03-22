package com.instacart.formula.rxjava3

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import io.reactivex.rxjava3.core.Observable

/**
 * Adapter which maps RxJava types to [Action] type. Take a look
 * at [RxAction.fromObservable].
 */
interface RxAction<Event : Any> : Action<Event> {
    companion object {
        /**
         * Creates an [Action] which will subscribe to an [Observable] created by factory
         * function [create].
         *
         * ```
         * RxAction.fromObservable { locationManager.updates() }.onEvent { event ->
         *   transition()
         * }
         * ```
         */
        inline fun <Event : Any> fromObservable(
            crossinline create: () -> Observable<Event>
        ): Action<Event> {
            return object : RxAction<Event> {

                override fun observable(): Observable<Event> {
                    return create()
                }

                override fun key(): Any = Unit
            }
        }

        /**
         * Creates an [Action] which will subscribe to an [Observable] created by factory
         * function [create].
         *
         * ```
         * RxAction.fromObservable(itemId) { repo.fetchItem(itemId) }.onEvent { event ->
         *   transition()
         * }
         * ```
         *
         * @param key Used to distinguish this [Action] from other actions.
         */
        inline fun <Event : Any> fromObservable(
            key: Any?,
            crossinline create: () -> Observable<Event>
        ): Action<Event> {
            return object : RxAction<Event> {

                override fun observable(): Observable<Event> {
                    return create()
                }

                override fun key(): Any? = key
            }
        }
    }

    fun observable(): Observable<Event>

    override fun start(send: (Event) -> Unit): Cancelable? {
        val disposable = observable().subscribe(send)
        return Cancelable(disposable::dispose)
    }
}

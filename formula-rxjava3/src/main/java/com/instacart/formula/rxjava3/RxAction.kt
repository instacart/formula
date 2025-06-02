package com.instacart.formula.rxjava3

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope

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
         * ```
         */
        fun <Event : Any> fromObservable(
            create: () -> Observable<Event>
        ): Action<Event> {
            return fromObservable(Unit, create)
        }

        /**
         * Creates an [Action] which will subscribe to an [Observable] created by factory
         * function [create].
         *
         * ```
         * RxAction.fromObservable(itemId) { repo.fetchItem(itemId) }.onEvent { event ->
         *   transition()
         * ```
         *
         * @param key Used to distinguish this [Action] from other actions.
         */
        fun <Event : Any> fromObservable(
            key: Any?,
            create: () -> Observable<Event>
        ): Action<Event> {
            return RxActionImpl(key, create)
        }
    }

    fun observable(): Observable<Event>

    override fun start(scope: CoroutineScope, send: (Event) -> Unit): Cancelable? {
        val disposable = observable().subscribe(send)
        return Cancelable(disposable::dispose)
    }
}

private data class RxActionImpl<Event : Any>(
    private val key: Any?,
    private val factory: () -> Observable<Event>
): RxAction<Event> {
    override fun observable(): Observable<Event> {
        return factory()
    }

    override fun key(): Any? {
        return key
    }
}

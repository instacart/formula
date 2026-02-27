package com.instacart.formula

import com.instacart.formula.action.FlowAction
import com.instacart.formula.action.LaunchCoroutineAction
import com.instacart.formula.action.StartEventAction
import com.instacart.formula.action.TerminateEventAction
import com.instacart.formula.utils.runCatchingCoroutines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A deferred action returned by [evaluation][Formula.evaluate] that will run for any new unique
 * value of [key] and will be be cleaned up if [key] changes or if [Action] is
 * left out of evaluation.
 *
 * Action can produce and send [events][Event] back to the [Formula] instance. It
 * can be used to subscribe to RxJava observables, Kotlin Flows, event buses, or any
 * other event mechanism.
 *
 * An [Action] key is a value that defines the identity of the [Action]. If
 * a key changes, the [Action] will be canceled and run again from a fresh state.
 * We also include the code call-site as a key parameter (concept called positional memoization).
 *
 * To construct a RxJava based action (using formula-rxjava3 library), you can do the following:
 *
 * ```kotlin
 * val action = RxAction.fromObservable { Observable.just(1, 2, 3) }
 * ```
 *
 * To use it within a [Formula]:
 * ```
 * Evaluation(
 *   actions = context.actions {
 *     action.onEvent { event ->
 *       transition()
 *     }
 *   }
 * )
 * ``
 *
 * @param Event A type of event used to notify [Formula].
 */
interface Action<Event> {
    companion object {
        private val INIT_ACTION = StartEventAction(Unit)

        /**
         * Emits an event when [Action] is initialized. You can use this action to send an event
         * when [Formula] is initialized.
         * ```
         * Action.onInit().onEvent {
         *   transition { analytics.trackViewEvent() }
         * }
         */
        fun onInit(): Action<Unit> {
            return INIT_ACTION
        }

        /**
         * Creates an [action][Action] which emits [data] event when it is initialized. It
         * uses [data] as key so it will emit a new event whenever [data] changes.
         *
         * ```
         * Action.onData(itemId).onEvent {
         *   transition { api.fetchItem(itemId) }
         * }
         * ```
         */
        fun <Data> onData(data: Data): Action<Data> {
            return StartEventAction(data)
        }

        /**
         * Creates an [action][Action] which emits an event when action is terminated. You
         * can use this to be notified when [Formula] is terminated. Note that transitions
         * to new state will be discarded because [Formula] is terminated. This event
         * is usually used to notify other services/analytics of [Formula] termination.
         *
         * ```
         * Action.onTerminate().onEvent {
         *   transition { analytics.trackCloseEvent() }
         * }
         * ```
         */
        fun onTerminate(): Action<Unit> {
            return TerminateEventAction
        }

        /**
         * Creates an [Action] that runs a [block] suspend function.
         */
        fun <Result> launch(
            context: CoroutineContext = EmptyCoroutineContext,
            block: suspend () -> Result,
        ): Action<Result> {
            return launch(
                context = context,
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
            context: CoroutineContext = EmptyCoroutineContext,
            block: suspend () -> Result,
        ): Action<Result> {
            return LaunchCoroutineAction(
                context = context,
                key = key,
                block = block,
            )
        }

        /**
         * Creates an [Action] that runs a [block] suspend function and catches any exceptions.
         */
        inline fun <Result> launchCatching(
            context: CoroutineContext = EmptyCoroutineContext,
            crossinline block: suspend () -> Result,
        ): Action<kotlin.Result<Result>> {
            return launchCatching(
                key = null,
                context = context,
                block = block,
            )
        }

        /**
         * Creates an [Action] that runs a [block] suspend function and catches any exceptions.
         */
        inline fun <Result> launchCatching(
            key: Any?,
            context: CoroutineContext = EmptyCoroutineContext,
            crossinline block: suspend () -> Result,
        ): Action<kotlin.Result<Result>> {
            return LaunchCoroutineAction(
                context = context,
                key = key,
                block = {
                    runCatchingCoroutines(block)
                },
            )
        }

        /**
         * Creates an [Action] which will launch a [Flow] created by factory function [create].
         *
         * @param context A [CoroutineContext] used to start the flow. By default, [EmptyCoroutineContext] is used.
         */
        fun <Event> fromFlow(
            context: CoroutineContext = EmptyCoroutineContext,
            create: suspend () -> Flow<Event>
        ): Action<Event> {
            return FlowAction(context, null, create)
        }

        /**
         * Creates an [Action] which will launch a [Flow] created by factory function [create].
         *
         * @param key Used to distinguish this [Action] from other actions.
         * @param context A [CoroutineContext] used to start the flow. By default, [EmptyCoroutineContext] is used.
         */
        fun <Event> fromFlow(
            key: Any?,
            context: CoroutineContext = EmptyCoroutineContext,
            create: suspend () -> Flow<Event>
        ): Action<Event> {
            return FlowAction(context, key, create)
        }
    }

    interface Emitter<Event> {
        fun onEvent(event: Event)
        fun onError(throwable: Throwable)
    }

    /**
     * Formula runtime calls this method to initialize an [Action] the first time it is returned
     * by the [evaluation][Evaluation]. We use [key] to identify unique actions and Formula
     * runtime keeps running the action until:
     * - This action is not returned in [Evaluation.actions]
     * - The [key] of an action changes
     * - Formula is terminated
     *
     * Formula runtime will call [Cancelable.cancel] to terminate the action which can be used
     * to clean up such as remove subscriptions to RxJava observables, Kotlin Flows, event buses,
     * etc.
     *
     * @param emitter Use emitter to send events back to [Formula].
     */
    fun start(
        scope: CoroutineScope,
        emitter: Emitter<Event>,
    ): Cancelable?

    /**
     * An identifier used to distinguish between different types of actions.
     */
    fun key(): Any?
}

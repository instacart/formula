package com.instacart.formula

import com.instacart.formula.batch.BatchScheduler
import kotlin.reflect.KClass

/**
 * Transition is a function that is called when an [Event] happens and produces a [Result] which
 * indicates what [Formula] should do in response to this event. It can contain a new [state][State]
 * object which will trigger [Formula.evaluate] to be called and/or a list of [effect][Effect]
 * executables which will be executed by the [FormulaRuntime]. If there was a state change, effects
 * will be executed after [Formula.evaluate] is called.
 */
fun interface Transition<in Input, State, in Event> {

    /**
     * Result is an object returned by [Transition.toResult] which indicates what
     * [Formula] should do in response to an event. It can contain a new [state][Stateful]
     * and/or a list of [effect][Effect] executables which will be executed by the [FormulaRuntime].
     * Usually, you should use [TransitionContext] to construct the [Result] object.
     */
    sealed class Result<out State> {

        /**
         * Stateful result returned by the transition indicates that we want to apply a
         * new state to our [Formula]. It can also return optional [effects] function
         * which will be executed by [FormulaRuntime] after the state change.
         *
         * @param state New state which will be applied to our [Formula].
         * @param effects Optional function which will be executed after the state change. This
         * function is the place to call listeners, log events, trigger network requests or
         * database writes, and etc.
         */
        data class Stateful<State>(
            val state: State,
            override val effects: List<Effect> = emptyList(),
        ) : Result<State>()

        /**
         * Only effects result returned by the transition indicates that we don't need to update
         * [Formula] state and only want [effects] function to be executed by [FormulaRuntime].
         *
         * @param effects A function which will be executed by [FormulaRuntime]. This function
         * is the place to call listeners, log events, trigger network requests or database
         * writes, and etc.
         */
        data class OnlyEffects(override val effects: List<Effect>) : Result<Nothing>()

        /**
         * None result returned by the transition indicates that [Formula] doesn't need to
         * do anything in response to an event.
         */
        data object None : Result<Nothing>() {
            override val effects: List<Effect> = emptyList()
        }

        /**
         * Effects is an optional function returned by [toResult] which will be executed by
         * [FormulaRuntime]. This function is the place to call listeners, log events, trigger
         * network requests or database writes, and etc.
         *
         * @see Effect
         */
        abstract val effects: List<Effect>
    }

    /**
     * Defines an execution model for the transition
     */
    sealed class ExecutionType

    /**
     * Immediate execution type will try to process the transition immediately on the thread that
     * it arrives on. This should be used for user events that need to be processed quickly such
     * as navigation.
     */
    data object Immediate : ExecutionType()

    /**
     * Background execution type will try to process the transition on a background thread. This
     * should be used for data events that might be expensive to process.
     */
    data object Background : ExecutionType()


    /**
     * NOTE: this is experimental feature, the APIs might change in the future.
     *
     * Batched execution type indicates that the event can be collected into a batch
     * and processed as part of a group.
     *
     * @param scheduler Scheduler used to batch these events. The [BatchScheduler.key] will
     * be used to group events together and [BatchScheduler.schedule] will be used to
     * schedule the execution of the batch.
     *
     * TODO: add dropPrevious If true, will drop the previous event if a new event
     * arrives before batch is processed.
     */
    data class Batched(
        val scheduler: BatchScheduler, // TODO: make optional with default?
//        val dropPrevious: Boolean = false,
    ): ExecutionType()

    /**
     * Called when an [Event] happens and returns a [Result] object which can indicate a state
     * change and/or some executable effects. Use [TransitionContext.none] if nothing should happen
     * as part of this event.
     */
    fun TransitionContext<Input, State>.toResult(event: Event): Result<State>

    /**
     * Transition type is used as part of the key to distinguish different transitions.
     */
    fun type(): KClass<*> {
        return this::class
    }
}
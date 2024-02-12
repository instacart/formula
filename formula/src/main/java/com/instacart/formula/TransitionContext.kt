package com.instacart.formula

import com.instacart.formula.internal.DelegateTransitionContext
import com.instacart.formula.internal.combine
import com.instacart.formula.internal.toResult

/**
 * Transition context provides the current [input], the current [state] and utilities to help
 * create [Transition.Result] within [Transition.toResult].
 */
interface TransitionContext<out Input, State> {

    val input: Input
    val state: State

    /**
     * Returns a result that indicates to do nothing as part of this event.
     */
    fun none(): Transition.Result<Nothing> {
        return Transition.Result.None
    }

    /**
     * Returns a result that contains a new [State] object.
     */
    fun <State> transition(
        state: State,
    ): Transition.Result.Stateful<State> {
        return Transition.Result.Stateful(state, emptyList())
    }

    /**
     * Returns a result that contains a new [State] object and optional effect
     * executable that will be executed after the state is updated.
     */
    fun <State> transition(
        state: State,
        effect: () -> Unit,
    ): Transition.Result.Stateful<State> {
        return transition(state, Effect.Main, effect)
    }

    /**
     * Returns a result that contains a new [State] object and optional effect
     * executable that will be executed after the state is updated.
     */
    fun <State> transition(
        state: State,
        effectType: Effect.Type,
        effect: () -> Unit,
    ): Transition.Result.Stateful<State> {
        val effectList = listOf(Effect(effectType, effect))
        return Transition.Result.Stateful(state, effectList)
    }

    /**
     * Returns a result that requests effect function to be executed.
     */
    fun transition(
        effect: () -> Unit,
    ): Transition.Result.OnlyEffects {
        return transition(Effect.Main, effect)
    }

    /**
     * Returns a result that requests effect function to be executed.
     */
    fun transition(
        effectType: Effect.Type,
        effect: () -> Unit,
    ): Transition.Result.OnlyEffects {
        val effectList = listOf(Effect(effectType, effect))
        return Transition.Result.OnlyEffects(effectList)
    }

    /**
     * Delegates to another [Transition] to provide the result.
     */
    fun <Event> delegate(transition: Transition<Input, State, Event>, event: Event): Transition.Result<State> {
        return transition.run { toResult(event) }
    }

    /**
     * Delegates to another [Transition] that has [Unit] event type to provide the result.
     */
    fun delegate(transition: Transition<Input, State, Unit>): Transition.Result<State> {
        return delegate(transition, Unit)
    }

    /**
     * Function used to chain multiple transitions together.
     */
    fun <Event> Transition.Result<State>.andThen(
        transition: Transition<Input, State, Event>,
        event: Event
    ): Transition.Result<State> {
        return when (this) {
            Transition.Result.None -> {
                transition.toResult(this@TransitionContext, event)
            }
            is Transition.Result.OnlyEffects -> {
                combine(this, transition.toResult(this@TransitionContext, event))
            }
            is Transition.Result.Stateful -> {
                combine(this, transition.toResult(DelegateTransitionContext(input, this.state), event))
            }
        }
    }

    /**
     * Function used to chain multiple transitions together.
     */
    fun Transition.Result<State>.andThen(
        transition: Transition<Input, State, Unit>,
    ): Transition.Result<State> {
        return andThen(transition, Unit)
    }

    /**
     * Function to chain updated state with another transition.
     */
    fun <Event> State.andThen(
        transition: Transition<Input, State, Event>,
        event: Event
    ): Transition.Result<State> {
        return transition(this).andThen(transition, event)
    }

    /**
     * Function to chain updated state with another transition.
     */
    fun State.andThen(transition: Transition<Input, State, Unit>): Transition.Result<State> {
        return transition(this).andThen(transition)
    }
}


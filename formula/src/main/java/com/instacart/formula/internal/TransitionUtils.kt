package com.instacart.formula.internal

import com.instacart.formula.Transition
import com.instacart.formula.TransitionContext

internal object TransitionUtils {

    fun isEmpty(result: Transition.Result<*>): Boolean {
        return result == Transition.Result.None
    }
}

/**
 * Combines only effects transition result with another transition result.
 */
internal fun <State> combine(
    result: Transition.Result.OnlyEffects,
    other: Transition.Result<State>
): Transition.Result<State> {
    return when(other) {
        Transition.Result.None -> {
            result
        }
        is Transition.Result.OnlyEffects -> {
            val combined = result.effects + other.effects
            Transition.Result.OnlyEffects(combined)
        }
        is Transition.Result.Stateful -> {
            val combined = result.effects + other.effects
            Transition.Result.Stateful(other.state, combined)
        }
    }
}

/**
 * Combines stateful result with the result of another transition.
 */
internal fun <State> combine(
    result: Transition.Result.Stateful<State>,
    other: Transition.Result<State>
): Transition.Result<State> {
    return when(other) {
        Transition.Result.None -> {
            result
        }
        is Transition.Result.OnlyEffects -> {
            val combined = result.effects + other.effects
            Transition.Result.Stateful(result.state, combined)
        }
        is Transition.Result.Stateful -> {
            val combined = result.effects + other.effects
            Transition.Result.Stateful(other.state, combined)
        }
    }
}

internal fun <Input, State, Event> Transition<Input, State, Event>.toResult(
    context: TransitionContext<Input, State>,
    event: Event
): Transition.Result<State> {
    return context.run { toResult(event) }
}
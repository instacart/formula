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
internal fun <State> TransitionContext<*, State>.combine(
    result: Transition.Result.OnlyEffects,
    other: Transition.Result<State>
): Transition.Result<State> {
    return when(other) {
        Transition.Result.None -> {
            result
        }
        is Transition.Result.OnlyEffects -> {
            transition {
                result.effects.execute()
                other.effects.execute()
            }
        }
        is Transition.Result.Stateful -> {
            transition(other.state) {
                result.effects.execute()
                other.effects?.execute()
            }
        }
    }
}

/**
 * Combines stateful result with the result of another transition.
 */
internal fun <State> TransitionContext<*, State>.combine(
    result: Transition.Result.Stateful<State>,
    other: Transition.Result<State>
): Transition.Result<State> {
    return when(other) {
        Transition.Result.None -> {
            result
        }
        is Transition.Result.OnlyEffects -> {
            transition(result.state) {
                result.effects?.execute()
                other.effects.execute()
            }
        }
        is Transition.Result.Stateful -> {
            transition(other.state) {
                result.effects?.execute()
                other.effects?.execute()
            }
        }
    }
}
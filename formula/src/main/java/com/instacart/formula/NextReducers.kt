@file:Suppress("NOTHING_TO_INLINE")

package com.instacart.formula

/**
 * Contains utils to help reducing to [Next] class. All reducer classes should extend this class.
 *
 * [State] - state type which gets transformed
 * [Effect] - type of effects that could be emitted.
 */
abstract class NextReducers<State, Effect> {
    protected inline fun reduce(crossinline modify: (State) -> Next<State, Effect>): NextReducer<State, Effect> {
        return {
            modify(it)
        }
    }

    protected inline fun withoutEffects(crossinline modify: (State) -> State): NextReducer<State, Effect> {
        return {
            modify(it).toNext()
        }
    }

    protected inline fun optionalEffect(crossinline effect: (State) -> Effect?): NextReducer<State, Effect> {
        return {
            it.toNextWithOptionalEffect(effect(it))
        }
    }

    protected inline fun onlyEffect(crossinline effect: (State) -> Effect): NextReducer<State, Effect> {
        return {
            it.toNextWithEffects(effect(it))
        }
    }

    protected inline fun onlyEffects(crossinline effect: (State) -> Set<Effect>): NextReducer<State, Effect> {
        return {
            it.toNextWithEffects(effect(it))
        }
    }

    /**
     * Next creation utils
     */
    protected inline fun State.toNext(): Next<State, Effect> {
        return Next(this, emptySet())
    }

    protected inline fun State.toNextWithEffects(vararg effect: Effect): Next<State, Effect> {
        return Next(this, setOf(*effect))
    }

    protected inline fun State.toNextWithEffects(effects: Set<Effect>): Next<State, Effect> {
        return Next(this, effects)
    }

    protected inline fun State.toNextWithOptionalEffect(effect: Effect?): Next<State, Effect> {
        return Next(this, effect?.let { setOf(it) }.orEmpty())
    }

    protected inline fun State.toNextWithOptionalEffects(effects: Set<Effect>?): Next<State, Effect> {
        return Next(this, effects.orEmpty())
    }
}

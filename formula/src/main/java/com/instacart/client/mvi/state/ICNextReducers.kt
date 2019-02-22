@file:Suppress("NOTHING_TO_INLINE")

package com.instacart.client.mvi.state

/**
 * Contains utils to help reducing to [ICNext] class. All reducer classes should extend this class.
 *
 * [State] - state type which gets transformed
 * [Effect] - type of effects that could be emitted.
 */
abstract class ICNextReducers<State, Effect> {
    protected inline fun reduce(crossinline modify: (State) -> ICNext<State, Effect>): NextReducer<State, Effect> {
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
    protected inline fun State.toNext(): ICNext<State, Effect> {
        return ICNext(this, emptySet())
    }

    protected inline fun State.toNextWithEffects(vararg effect: Effect): ICNext<State, Effect> {
        return ICNext(this, setOf(*effect))
    }

    protected inline fun State.toNextWithEffects(effects: Set<Effect>): ICNext<State, Effect> {
        return ICNext(this, effects)
    }

    protected inline fun State.toNextWithOptionalEffect(effect: Effect?): ICNext<State, Effect> {
        return ICNext(this, effect?.let { setOf(it) }.orEmpty())
    }

    protected inline fun State.toNextWithOptionalEffects(effects: Set<Effect>?): ICNext<State, Effect> {
        return ICNext(this, effects.orEmpty())
    }
}

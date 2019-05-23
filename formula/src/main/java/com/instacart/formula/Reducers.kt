@file:Suppress("NOTHING_TO_INLINE")

package com.instacart.formula

/**
 * Contains utils to help reducing to [Next] class. All reducer classes should extend this class.
 *
 * @param [State] state type which gets transformed
 * @param [Effect] type of effects that could be emitted.
 *
 * Ex:
 * ```
 * class Modifications: Reducers<Int, ClearState>() {
 *  fun onAction(it: Action) = reduce { state ->
 *      val newState = when (it) {
 *          is Action.Increment -> state + 1
 *          is Action.Decrement -> state - 1
 *      }
 *      newState.toNextWithOptionalEffect(ClearState.takeIf { newState == 3 })
 *  }
 *
 *  fun onClearState(action: ClearState) = withoutEffects {
 *      0
 *  }
 * }
 * ```
 */
abstract class Reducers<State, Effect> {
    protected inline fun reduce(crossinline modify: (State) -> Next<State, Effect>): NextReducer<State, Effect> {
        return {
            modify(it)
        }
    }

    protected inline fun withoutEffects(crossinline modify: (State) -> State): NextReducer<State, Effect> {
        return {
            modify(it).noEffects()
        }
    }

    protected inline fun optionalEffect(crossinline effect: (State) -> Effect?): NextReducer<State, Effect> {
        return {
            it.withOptionalEffect(effect(it))
        }
    }

    protected inline fun onlyEffect(crossinline effect: (State) -> Effect): NextReducer<State, Effect> {
        return {
            it.withEffects(effect(it))
        }
    }

    protected inline fun onlyEffects(crossinline effect: (State) -> Set<Effect>): NextReducer<State, Effect> {
        return {
            it.withEffects(effect(it))
        }
    }

    /**
     * Next creation utils
     */
    protected inline fun State.noEffects(): Next<State, Effect> {
        return Next(this, emptySet())
    }

    protected inline fun State.withEffects(vararg effect: Effect): Next<State, Effect> {
        return Next(this, setOf(*effect))
    }

    protected inline fun State.withEffects(effects: Set<Effect>): Next<State, Effect> {
        return Next(this, effects)
    }

    protected inline fun State.withOptionalEffect(effect: Effect?): Next<State, Effect> {
        return Next(this, effect?.let { setOf(it) }.orEmpty())
    }

    protected inline fun State.withOptionalEffects(effects: Set<Effect>?): Next<State, Effect> {
        return Next(this, effects.orEmpty())
    }

    @Deprecated("Use noEffects()", replaceWith = ReplaceWith("noEffects()"))
    protected inline fun State.toNext(): Next<State, Effect> {
        return noEffects()
    }

    @Deprecated("Use withEffects()", replaceWith = ReplaceWith("withEffects(*effect)"))
    protected inline fun State.toNextWithEffects(vararg effect: Effect): Next<State, Effect> {
        return withEffects(*effect)
    }

    @Deprecated("Use withEffects()", replaceWith = ReplaceWith("withEffects(effects)"))
    protected inline fun State.toNextWithEffects(effects: Set<Effect>): Next<State, Effect> {
        return withEffects(effects)
    }

    @Deprecated("Use withOptionalEffect()", replaceWith = ReplaceWith("withOptionalEffect(effect)"))
    protected inline fun State.toNextWithOptionalEffect(effect: Effect?): Next<State, Effect> {
        return withOptionalEffect(effect)
    }

    @Deprecated("Use withOptionalEffects()", replaceWith = ReplaceWith("withOptionalEffects(effects)"))
    protected inline fun State.toNextWithOptionalEffects(effects: Set<Effect>?): Next<State, Effect> {
        return withOptionalEffects(effects)
    }
}

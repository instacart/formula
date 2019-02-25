package com.instacart.formula.integration

import io.reactivex.Flowable

private fun <Key> toLifecycleEffect(): (ActiveKeys<Key>) -> Flowable<LifecycleEvent<Key>> {
    var lastState: ActiveKeys<Key>? = null
    return { state ->
        val effects = ActiveKeys.findLifecycleEffects(
            lastState = lastState,
            currentState = state
        )

        lastState = state
        Flowable.fromIterable(effects)
    }
}

private fun <Key> Flowable<ActiveKeys<Key>>.lifecycleEffects(): Flowable<LifecycleEvent<Key>> {
    return this
        .distinctUntilChanged()
        .flatMap(toLifecycleEffect())
}

/**
 * Creates mvi updates stream for a specified contract type.
 * It takes a type and a state stream factory.
 */
fun <Key, State> Flowable<ActiveKeys<Key>>.createStateUpdates(
    type: Class<Key>, init: (Key) -> Flowable<State>
): Flowable<KeyState<Key, State>> {
    return this
        .lifecycleEffects()
        .filter { type.isInstance(it.key) }
        .groupBy { it.key }
        .flatMap {
            it.switchMap {
                val contract = it.key as Key
                when (it) {
                    is LifecycleEvent.Attach -> init(contract).map { state ->
                        KeyState(contract, state)
                    }
                    is LifecycleEvent.Detach -> Flowable.empty()
                }
            }
        }
}

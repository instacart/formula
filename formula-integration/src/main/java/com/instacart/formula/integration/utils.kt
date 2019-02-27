package com.instacart.formula.integration

import io.reactivex.Flowable

private fun <Key> toLifecycleEffect(): (BackStack<Key>) -> Flowable<LifecycleEvent<Key>> {
    var lastState: BackStack<Key>? = null
    return { state ->
        val effects = BackStack.findLifecycleEffects(
            lastState = lastState,
            currentState = state
        )

        lastState = state
        Flowable.fromIterable(effects)
    }
}

private fun <Key> Flowable<BackStack<Key>>.lifecycleEffects(): Flowable<LifecycleEvent<Key>> {
    return this
        .distinctUntilChanged()
        .flatMap(toLifecycleEffect())
}

/**
 * Listens for back stack changes and initializes a render loop
 * when a specified type of key is added to the back stack.
 *
 * [type] - key class
 * [init] - a function that initializes the render model [Flowable]
 */
fun <Key, State> Flowable<BackStack<Key>>.createStateUpdates(
    type: Class<Key>,
    init: (Key) -> Flowable<State>
): Flowable<KeyState<Key, State>> {
    return this
        .lifecycleEffects()
        .filter { type.isInstance(it.key) }
        .groupBy { it.key }
        .flatMap {
            it.switchMap {
                val contract = it.key as Key
                when (it) {
                    is LifecycleEvent.Added -> init(contract).map { state ->
                        KeyState(contract, state)
                    }
                    is LifecycleEvent.Removed -> Flowable.empty()
                }
            }
        }
}

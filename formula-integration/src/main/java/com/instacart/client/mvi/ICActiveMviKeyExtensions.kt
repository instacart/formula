package com.instacart.client.mvi

import com.instacart.formula.ICMviState
import io.reactivex.Flowable

private fun <Key> toLifecycleEffect(): (ICActiveMviKeys<Key>) -> Flowable<ICMviLifecycleEvent<Key>> {
    var lastState: ICActiveMviKeys<Key>? = null
    return { state ->
        val effects = ICActiveMviKeys.findLifecycleEffects(
            lastState = lastState,
            currentState = state
        )

        lastState = state
        Flowable.fromIterable(effects)
    }
}

fun <Key> Flowable<ICActiveMviKeys<Key>>.lifecycleEffects(): Flowable<ICMviLifecycleEvent<Key>> {
    return this
        .distinctUntilChanged()
        .flatMap(toLifecycleEffect())
}

/**
 * Creates mvi updates stream for a specified contract type.
 * It takes a type and a state stream factory.
 */
fun <Key, State> Flowable<ICActiveMviKeys<Key>>.createStateUpdates(
    type: Class<Key>, init: (Key) -> Flowable<State>
): Flowable<ICMviState<Key, State>> {
    return this
        .lifecycleEffects()
        .filter { type.isInstance(it.key) }
        .groupBy { it.key }
        .flatMap {
            it.switchMap {
                val contract = it.key as Key
                when (it) {
                    is ICMviLifecycleEvent.Attach -> init(contract).map { state ->
                        ICMviState(contract, state)
                    }
                    is ICMviLifecycleEvent.Detach -> Flowable.empty()
                }
            }
        }
}

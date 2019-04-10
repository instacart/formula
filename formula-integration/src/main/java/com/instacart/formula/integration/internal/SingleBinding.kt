package com.instacart.formula.integration.internal

import com.instacart.formula.integration.BackStack
import com.instacart.formula.integration.Binding
import com.instacart.formula.integration.KeyState
import com.instacart.formula.integration.LifecycleEvent
import io.reactivex.Flowable

internal class SingleBinding<Key, Scope, State>(
    private val type: Class<Key>,
    private val init: (Scope, Key) -> Flowable<State>
) : Binding<Scope, Key, State>() {
    /**
     * Helper method to select state from active backstack.
     */
    override fun state(component: Scope, backstack: Flowable<BackStack<Key>>): Flowable<KeyState<Key, State>> {
        return backstack.createStateUpdates(type) { key ->
            init(component, key)
        }
    }

    override fun binds(key: Any): Boolean {
        return type.isInstance(key)
    }

    /**
     * Listens for back stack changes and initializes a render loop
     * when a specified type of key is added to the back stack.
     *
     * @param type key class
     * @param init a function that initializes the render model [Flowable]
     */
    private fun <Key, State> Flowable<BackStack<Key>>.createStateUpdates(
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
        return this.distinctUntilChanged().flatMap(toLifecycleEffect())
    }
}

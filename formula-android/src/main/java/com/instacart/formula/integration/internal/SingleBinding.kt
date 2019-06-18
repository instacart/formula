package com.instacart.formula.integration.internal

import com.instacart.formula.integration.BackStack
import com.instacart.formula.integration.Binding
import com.instacart.formula.integration.Integration
import com.instacart.formula.integration.KeyState
import com.instacart.formula.integration.LifecycleEvent
import io.reactivex.Observable

/**
 * Defines how a specific key should be bound to it's [Integration],
 */
internal class SingleBinding<Component, Key, State : Any>(
    private val type: Class<Key>,
    private val integration: Integration<Component, Key, State>
) : Binding<Component, Key>() {
    /**
     * Helper method to select state from active backstack.
     */
    override fun state(component: Component, backstack: Observable<BackStack<Key>>): Observable<KeyState<Key>> {
        return backstack.createStateUpdates(type) { key ->
            integration.create(component, key)
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
     * @param init a function that initializes the render model [Observable]
     */
    private fun <Key, State : Any> Observable<BackStack<Key>>.createStateUpdates(
        type: Class<Key>,
        init: (Key) -> Observable<State>
    ): Observable<KeyState<Key>> {
        return this
            .lifecycleEffects()
            .filter { type.isInstance(it.key) }
            .groupBy { it.key }
            .flatMap {
                it.switchMap {
                    val contract = it.key
                    when (it) {
                        is LifecycleEvent.Added -> init(contract).map { state ->
                            KeyState(contract, state)
                        }
                        is LifecycleEvent.Removed -> Observable.empty()
                    }
                }
            }
    }

    private fun <Key> toLifecycleEffect(): (BackStack<Key>) -> Observable<LifecycleEvent<Key>> {
        var lastState: BackStack<Key>? = null
        return { state ->
            val effects = BackStackUtils.findLifecycleEffects(
                lastState = lastState,
                currentState = state
            )

            lastState = state
            Observable.fromIterable(effects)
        }
    }

    private fun <Key> Observable<BackStack<Key>>.lifecycleEffects(): Observable<LifecycleEvent<Key>> {
        return this.distinctUntilChanged().flatMap(toLifecycleEffect())
    }
}

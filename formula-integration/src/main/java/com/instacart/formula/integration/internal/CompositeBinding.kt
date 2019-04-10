package com.instacart.formula.integration.internal

import com.instacart.formula.integration.BackStack
import com.instacart.formula.integration.DisposableScope
import com.instacart.formula.integration.KeyBinding
import com.instacart.formula.integration.KeyState
import io.reactivex.Flowable

class CompositeBinding<Scope, Key : Any, ChildScope>(
    private val scopeFactory: (Scope) -> DisposableScope<ChildScope>,
    private val bindings: List<KeyBinding<ChildScope, Key, *>>
) : KeyBinding<Scope, Key, Any>() {

    override fun binds(key: Any): Boolean {
        return bindings.any { it.binds(key) }
    }

    /**
     * Helper method to select state from active store.
     */
    override fun state(scope: Scope, store: Flowable<BackStack<Key>>): Flowable<KeyState<Key, Any>> {
        return store
            .map {
                it.keys.any { key ->
                    binds(key)
                }
            }
            .distinctUntilChanged()
            .switchMap { enterScope ->
                if (enterScope) {
                    val disposableScope = scopeFactory.invoke(scope)
                    val updates = bindings.map {
                        it.state(disposableScope.component, store)
                    }

                    Flowable.merge(updates)
                        .doOnCancel { disposableScope.dispose() } as Flowable<KeyState<Key, Any>>
                } else {
                    Flowable.empty<KeyState<Key, Any>>()
                }
            }
    }
}

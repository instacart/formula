package com.instacart.formula.integration.internal

import com.instacart.formula.integration.BackStack
import com.instacart.formula.integration.DisposableScope
import com.instacart.formula.integration.Binding
import com.instacart.formula.integration.KeyState
import io.reactivex.Flowable

/**
 * @param Key A key type associated with this binding.
 * @param ParentComponent A component associated with the parent. Often this will map to the parent dagger component.
 * @param ScopedComponent A component that is initialized when user enters this flow and is shared between
 *                  all the screens within the flow. Component will be destroyed when user exists the flow.
 */
class CompositeBinding<Key: Any, ParentComponent, ScopedComponent>(
    private val scopeFactory: (ParentComponent) -> DisposableScope<ScopedComponent>,
    private val bindings: List<Binding<ScopedComponent, Key, *>>
) : Binding<ParentComponent, Key, Any>() {

    override fun binds(key: Any): Boolean {
        return bindings.any { it.binds(key) }
    }

    /**
     * Helper method to select state from active store.
     */
    override fun state(component: ParentComponent, store: Flowable<BackStack<Key>>): Flowable<KeyState<Key, Any>> {
        return store
            .isInScope()
            .switchMap { enterScope ->
                if (enterScope) {
                    val disposableScope = scopeFactory.invoke(component)
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

    /**
     * Defines if any of the binding are in the backstack. We use this to initialize the
     */
    private fun Flowable<BackStack<Key>>.isInScope(): Flowable<Boolean> {
        return map {
            it.keys.any { key ->
                binds(key)
            }
        }.distinctUntilChanged()
    }
}

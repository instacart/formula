package com.instacart.formula.integration.internal

import com.instacart.formula.integration.BackStack
import com.instacart.formula.integration.Binding
import com.instacart.formula.integration.ComponentFactory
import com.instacart.formula.integration.KeyState
import io.reactivex.Flowable
import io.reactivex.Observable

/**
 * Defines how a group of keys should be bound to their integrations.
 *
 * @param Key A key type associated with this binding.
 * @param ParentComponent A component associated with the parent. Often this will map to the parent dagger component.
 * @param ScopedComponent A component that is initialized when user enters this flow and is shared between
 *                  all the screens within the flow. Component will be destroyed when user exists the flow.
 */
internal class CompositeBinding<Key: Any, ParentComponent, ScopedComponent>(
    private val scopeFactory: ComponentFactory<ParentComponent, ScopedComponent>,
    private val bindings: List<Binding<ScopedComponent, Key>>
) : Binding<ParentComponent, Key>() {

    override fun binds(key: Any): Boolean {
        return bindings.any { it.binds(key) }
    }

    override fun state(component: ParentComponent, backstack: Observable<BackStack<Key>>): Observable<KeyState<Key>> {
        return backstack
            .isInScope()
            .switchMap { enterScope ->
                if (enterScope) {
                    val disposableScope = scopeFactory.invoke(component)
                    val updates = bindings.map {
                        it.state(disposableScope.component, backstack)
                    }

                    Observable.merge(updates).doOnDispose { disposableScope.dispose() }
                } else {
                    Observable.empty<KeyState<Key>>()
                }
            }
    }

    /**
     * Defines if any of the binding are in the backstack. We use this to initialize the
     */
    private fun Observable<BackStack<Key>>.isInScope(): Observable<Boolean> {
        return map {
            it.keys.any { key ->
                binds(key)
            }
        }.distinctUntilChanged()
    }
}

package com.instacart.formula.integration

import io.reactivex.Flowable
import kotlin.reflect.KClass

sealed class KeyBinding<Scope, Key, State> {
    /**
     * Determines if this binding handles the key
     */
    abstract fun binds(key: Any): Boolean

    abstract fun state(scope: Scope, store: Flowable<BackStack<Key>>): Flowable<KeyState<Key, State>>

    class Binding<Key, Scope, State>(
        val type: Class<Key>,
        val init: (Scope, Key) -> Flowable<State>
    ) : KeyBinding<Scope, Key, State>() {
        /**
         * Helper method to select state from active store.
         */
        override fun state(scope: Scope, store: Flowable<BackStack<Key>>): Flowable<KeyState<Key, State>> {
            return store.createStateUpdates(type) { key ->
                init(scope, key)
            }
        }

        override fun binds(key: Any): Boolean {
            return type.isInstance(key)
        }
    }

    class CompositeBinding<Scope, Key : Any, ChildScope>(
        val scopeFactory: (Scope) -> DisposableScope<ChildScope>,
        val bindings: List<KeyBinding<ChildScope, Key, *>>
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

                        Flowable
                            .merge(updates)
                            .doOnCancel { disposableScope.dispose() } as Flowable<KeyState<Key, Any>>
                    } else {
                        Flowable.empty<KeyState<Key, Any>>()
                    }
                }
        }
    }

    class Builder<ParentScope, Scope, Key : Any>(private val scopeFactory: (ParentScope) -> DisposableScope<Scope>) {
        private val bindings: MutableList<KeyBinding<Scope, Key, *>> = mutableListOf()

        /**
         * Binds specific type of key to the render model management.
         */
        fun <T : Key, S> register(type: KClass<T>, init: (T) -> Flowable<S>): Builder<ParentScope, Scope, Key> {
            bindings.add(
                Binding<T, Scope, S>(
                    type.java,
                    init = { scope, key ->
                        init(key)
                    }) as Binding<Key, Scope, S>
            )
            return this
        }

        /**
         * Binds specific type of key to the render model management.
         */
        fun <T : Key, S> bind(type: KClass<T>, init: (Scope, T) -> Flowable<S>): Builder<ParentScope, Scope, Key> {
            return bind((Binding(
                type.java,
                init
            ) as Binding<Key, Scope, S>))
        }

        fun bind(binding: Binding<Key, Scope, *>): Builder<ParentScope, Scope, Key> {
            bindings.add(binding)
            return this
        }

        fun <ChildScope> withScope(
            scopeFactory: (Scope) -> DisposableScope<ChildScope>,
            init: Builder<Scope, ChildScope, Key>.() -> Unit
        ): Builder<ParentScope, Scope, Key> {
            val scoped = Builder<Scope, ChildScope, Key>(scopeFactory)
                .apply(init)
                .build()
            return bind(scoped)
        }

        fun bind(compositeBinding: CompositeBinding<Scope, Key, *>): Builder<ParentScope, Scope, Key> {
            bindings.add(compositeBinding)
            return this
        }

        fun build(): CompositeBinding<ParentScope, Key, Scope> {
            return CompositeBinding(scopeFactory, bindings)
        }
    }
}

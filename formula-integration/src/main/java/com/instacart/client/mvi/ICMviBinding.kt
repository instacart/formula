package com.instacart.client.mvi

import arrow.syntax.function.partially1
import com.instacart.client.di.scopes.DisposableScope
import com.instacart.formula.ICMviState
import io.reactivex.Flowable
import kotlin.reflect.KClass

sealed class ICMviBinding<Scope, Key, State> {
    /**
     * Determines if this binding handles the key
     */
    abstract fun binds(key: Any): Boolean

    abstract fun state(scope: Scope, store: Flowable<ICActiveMviKeys<Key>>): Flowable<ICMviState<Key, State>>

    class Binding<Key, Scope, State>(
        val type: Class<Key>,
        val init: (Scope, Key) -> Flowable<State>
    ) : ICMviBinding<Scope, Key, State>() {
        /**
         * Helper method to select state from active store.
         */
        override fun state(scope: Scope, store: Flowable<ICActiveMviKeys<Key>>): Flowable<ICMviState<Key, State>> {
            return store.createStateUpdates(type, init.partially1(scope))
        }

        override fun binds(key: Any): Boolean {
            return type.isInstance(key)
        }
    }

    class CompositeBinding<Scope, Key : Any, ChildScope>(
        val scopeFactory: (Scope) -> DisposableScope<ChildScope>,
        val bindings: List<ICMviBinding<ChildScope, Key, *>>
    ) : ICMviBinding<Scope, Key, Any>() {

        override fun binds(key: Any): Boolean {
            return bindings.any { it.binds(key) }
        }

        /**
         * Helper method to select state from active store.
         */
        override fun state(scope: Scope, store: Flowable<ICActiveMviKeys<Key>>): Flowable<ICMviState<Key, Any>> {
            return store
                .map {
                    it.activeKeys.any { key ->
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
                            .doOnCancel { disposableScope.dispose() } as Flowable<ICMviState<Key, Any>>
                    } else {
                        Flowable.empty<ICMviState<Key, Any>>()
                    }
                }
        }
    }

    class Builder<ParentScope, Scope, Key : Any>(private val scopeFactory: (ParentScope) -> DisposableScope<Scope>) {
        private val bindings: MutableList<ICMviBinding<Scope, Key, *>> = mutableListOf()

        /**
         * For specific mvi contract, provide state stream factory.
         */
        fun <T : Key, S> register(type: KClass<T>, init: (T) -> Flowable<S>): Builder<ParentScope, Scope, Key> {
            bindings.add(Binding<T, Scope, S>(type.java, init = { scope, key ->
                init(key)
            }) as Binding<Key, Scope, S>)
            return this
        }

        /**
         * For specific mvi contract, provide state stream factory.
         */
        fun <T : Key, S> bind(type: KClass<T>, init: (Scope, T) -> Flowable<S>): Builder<ParentScope, Scope, Key> {
            return bind((Binding(type.java, init) as Binding<Key, Scope, S>))
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

package com.instacart.formula.integration

import com.instacart.formula.integration.internal.SingleBinding
import com.instacart.formula.integration.internal.CompositeBinding
import io.reactivex.Flowable
import kotlin.reflect.KClass

abstract class KeyBinding<Scope, Key, State> {
    /**
     * Determines if this binding handles the key
     */
    abstract fun binds(key: Any): Boolean

    abstract fun state(scope: Scope, store: Flowable<BackStack<Key>>): Flowable<KeyState<Key, State>>

    class Builder<ParentScope, Scope, Key : Any>(private val scopeFactory: (ParentScope) -> DisposableScope<Scope>) {
        private val bindings: MutableList<KeyBinding<Scope, Key, *>> = mutableListOf()

        inline fun <reified T : Key, S> register(noinline init: (T) -> Flowable<S>) = register(T::class, init)

        /**
         * Binds specific type of key to the render model management.
         */
        fun <T : Key, S> register(type: KClass<T>, init: (T) -> Flowable<S>): Builder<ParentScope, Scope, Key> {
            bindings.add(
                SingleBinding<T, Scope, S>(
                    type.java,
                    init = { scope, key ->
                        init(key)
                    }) as SingleBinding<Key, Scope, S>
            )
            return this
        }

        /**
         * Binds specific type of key to the render model management.
         */
        fun <T : Key, S> bind(type: KClass<T>, init: (Scope, T) -> Flowable<S>): Builder<ParentScope, Scope, Key> {
            return bind((SingleBinding(
                type.java,
                init
            ) as SingleBinding<Key, Scope, S>))
        }

        fun bind(binding: SingleBinding<Key, Scope, *>): Builder<ParentScope, Scope, Key> {
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

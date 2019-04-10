package com.instacart.formula.integration

import com.instacart.formula.integration.internal.CompositeBinding
import com.instacart.formula.integration.internal.SingleBinding
import io.reactivex.Flowable
import kotlin.reflect.KClass

/**
 * Defines how specific keys bind to the state management associated
 */
abstract class Binding<ParentComponent, Key, State> {
    companion object {
        fun <Component, Key : Any, State : Any> single(
            type: KClass<Key>,
            stateInit: (Component, Key) -> Flowable<State>
        ): Binding<Component, Key, *> {
            return SingleBinding(type.java, stateInit)
        }
    }

    class Builder<ParentComponent, Component, Key : Any>(
        private val componentFactory: ComponentFactory<ParentComponent, Component>
    ) {
        private val bindings: MutableList<Binding<Component, Key, *>> = mutableListOf()

        fun bind(binding: Binding<Component, Key, *>) = apply {
            bindings.add(binding)
        }

        inline fun <reified T : Key, S : Any> register(noinline init: (T) -> Flowable<S>) = register(T::class, init)

        /**
         * Binds specific type of key to the render model management.
         */
        fun <T : Key, S : Any> register(type: KClass<T>, init: (T) -> Flowable<S>) = apply {
            val initWithScope = { scope: Component, key: T ->
                init(key)
            }
            bind(type, initWithScope)
        }

        /**
         * Binds specific type of key to the render model management.
         */
        fun <T : Key, S : Any> bind(type: KClass<T>, init: (Component, T) -> Flowable<S>) = apply {
            bind(single(type, init) as Binding<Component, Key, S>)
        }

        fun <NewComponent> withScope(
            scopeFactory: ComponentFactory<Component, NewComponent>,
            init: Builder<Component, NewComponent, Key>.() -> Unit
        ) = apply {
            val scoped = Builder<Component, NewComponent, Key>(scopeFactory).apply(init).build()
            bind(scoped)
        }

        inline fun <reified T : Key> bind(integration: Integration<T, *, *>) = apply {
            val init: (T) -> Flowable<Any> = integration::init as (T) -> Flowable<Any>
            register(T::class, init)
        }

        fun build(): Binding<ParentComponent, Key, Any> {
            return CompositeBinding(componentFactory, bindings)
        }
    }

    /**
     * Returns true if this binding handles this [key]
     */
    abstract fun binds(key: Any): Boolean

    /**
     * @param component A component associated with the parent. Often this will map to the parent dagger component.
     * @param backstack A stream that emits the current back stack state.
     */
    abstract fun state(component: ParentComponent, backstack: Flowable<BackStack<Key>>): Flowable<KeyState<Key>>
}

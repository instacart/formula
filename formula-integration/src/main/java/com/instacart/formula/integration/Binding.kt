package com.instacart.formula.integration

import com.instacart.formula.integration.internal.SingleBinding
import io.reactivex.Flowable
import kotlin.reflect.KClass

/**
 * Defines how specific keys bind to the state management associated
 */
abstract class Binding<in ParentComponent, Key> {
    companion object {
        fun <Component, Key : Any, State : Any> single(
            type: KClass<Key>,
            stateInit: (Component, Key) -> Flowable<State>
        ): Binding<Component, Key> {
            return SingleBinding(type.java, stateInit)
        }
    }

    /**
     * Returns true if this binding handles this [key]
     */
    abstract fun binds(key: Any): Boolean

    /**
     * Listens to the back stack changes and returns a stream of [KeyState] updates for keys that it [binds] to.
     *
     * @param component A component associated with the parent. Often this will map to the parent dagger component.
     * @param backstack A stream that emits the current back stack state.
     */
    abstract fun state(component: ParentComponent, backstack: Flowable<BackStack<Key>>): Flowable<KeyState<Key>>
}

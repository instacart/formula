package com.instacart.formula.integration

import arrow.core.Option
import com.instacart.formula.internal.mapNotNull
import io.reactivex.Flowable

/**
 * A simple store that allows you to handle multiple
 * mvi contracts and their states.
 */
class FlowStore<Key : Any> constructor(
    keyState: Flowable<BackStack<Key>>,
    private val root: KeyBinding.CompositeBinding<Unit, Key, Unit>
) {
    companion object {
        inline fun <Key : Any> init(
            state: Flowable<BackStack<Key>>,
            crossinline init: KeyBinding.Builder<Unit, Unit, Key>.() -> Unit
        ): FlowStore<Key> {
            val root = KeyBinding.Builder<Unit, Unit, Key>(scopeFactory = {
                DisposableScope(component = Unit, onDispose = {})
            })
                .apply(init)
                .build()

            return FlowStore(state, root)
        }
    }

    private val reducerFactory = FlowReducers(root)
    private val keyState = keyState.replay(1).refCount()

    fun state(): Flowable<FlowState<Key>> {
        val backstackChangeReducer = keyState.map(reducerFactory::onBackstackChange)
        val stateChangeReducers = root.state(Unit, keyState).map(reducerFactory::onScreenStateChanged)

        val reducers = Flowable.merge(backstackChangeReducer, stateChangeReducers)

        return reducers
            .scan(FlowState<Key>()) { state, reducer ->
                reducer(state)
            }
            .distinctUntilChanged()
    }

    fun screen(): Flowable<Option<KeyState<Key, *>>> {
        return state().map { it.currentScreenState() }.distinctUntilChanged()
    }

    fun notNullScreen(): Flowable<KeyState<Key, *>> {
        return screen().mapNotNull { it.orNull() }
    }
}

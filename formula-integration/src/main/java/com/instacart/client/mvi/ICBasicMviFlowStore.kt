package com.instacart.client.mvi

import arrow.core.Option
import com.instacart.client.core.rx.mapNotNull
import com.instacart.client.di.scopes.DisposableScope
import com.instacart.formula.ICMviState
import io.reactivex.Flowable

/**
 * A simple store that allows you to handle multiple
 * mvi contracts and their states.
 */
class ICBasicMviFlowStore<Key : Any> constructor(
    keyState: Flowable<ICActiveMviKeys<Key>>,
    private val root: ICMviBinding.CompositeBinding<Unit, Key, Unit>
) {
    companion object {
        inline fun <Key : Any> init(
            state: Flowable<ICActiveMviKeys<Key>>,
            crossinline init: ICMviBinding.Builder<Unit, Unit, Key>.() -> Unit
        ): ICBasicMviFlowStore<Key> {
            val root = ICMviBinding.Builder<Unit, Unit, Key>(scopeFactory = {
                DisposableScope(component = Unit, onDispose = {})
            })
                .apply(init)
                .build()

            return ICBasicMviFlowStore(state, root)
        }
    }

    private val reducerFactory = ICBasicMviFlowReducers(root)
    private val keyState = keyState.replay(1).refCount()

    private fun state(): Flowable<ICBasicMviFlowState<Key>> {
        val backstackChangeReducer = keyState.map(reducerFactory::onBackstackChange)
        val stateChangeReducers = root.state(Unit, keyState).map(reducerFactory::onScreenStateChanged)

        val reducers = Flowable.merge(backstackChangeReducer, stateChangeReducers)

        return reducers
            .scan(ICBasicMviFlowState<Key>()) { state, reducer ->
                reducer(state)
            }
            .distinctUntilChanged()
    }

    fun screen(): Flowable<Option<ICMviState<Key, *>>> {
        return state().map { it.currentScreenState() }.distinctUntilChanged()
    }

    fun notNullScreen(): Flowable<ICMviState<Key, *>> {
        return screen().mapNotNull { it.orNull() }
    }
}

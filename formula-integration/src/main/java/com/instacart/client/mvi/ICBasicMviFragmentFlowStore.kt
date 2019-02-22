package com.instacart.client.mvi

import com.instacart.client.mvi.backstack.ICStateMachine

/**
 * A simple store that allows you to handle multiple
 * mvi contracts and their states.
 */
class ICBasicMviFragmentFlowStore(
    private val contractStore: ICStateMachine<ICMviFragmentContract<*>>,
    private val store: ICBasicMviFlowStore<ICMviFragmentContract<*>>
) {
    companion object {
        inline fun init(crossinline init: ICMviBinding.Builder<Unit, Unit, ICMviFragmentContract<*>>.() -> Unit): ICBasicMviFragmentFlowStore {
            val contractStore = ICStateMachine<ICMviFragmentContract<*>>()
            val store = ICBasicMviFlowStore.init(contractStore.stateChanges(), init)
            return ICBasicMviFragmentFlowStore(contractStore, store)
        }
    }

    fun onLifecycleEffect(event: ICMviLifecycleEvent<ICMviFragmentContract<*>>) {
        contractStore.onLifecycleEffect(event)
    }

    fun screen() = store.screen()
}
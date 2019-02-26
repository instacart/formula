package com.instacart.formula.fragment

import arrow.core.Option
import com.instacart.formula.integration.BackStackStore
import com.instacart.formula.integration.FlowState
import com.instacart.formula.integration.FlowStore
import com.instacart.formula.integration.KeyBinding
import com.instacart.formula.integration.KeyState
import com.instacart.formula.integration.LifecycleEvent
import io.reactivex.Flowable

/**
 * A simple store that allows you to handle multiple
 * fragment contracts and their states.
 */
class FragmentFlowStore(
    private val contractStore: BackStackStore<FragmentContract<*>>,
    private val store: FlowStore<FragmentContract<*>>
) {
    companion object {
        inline fun init(crossinline init: KeyBinding.Builder<Unit, Unit, FragmentContract<*>>.() -> Unit): FragmentFlowStore {
            val contractStore = BackStackStore<FragmentContract<*>>()
            val store = FlowStore.init(contractStore.stateChanges(), init)
            return FragmentFlowStore(contractStore, store)
        }
    }

    fun onLifecycleEffect(event: LifecycleEvent<FragmentContract<*>>) {
        contractStore.onLifecycleEffect(event)
    }

    fun state(): Flowable<FragmentFlowState> {
        return store.state()
    }

    fun screen(): Flowable<Option<KeyState<FragmentContract<*>, *>>> {
        return store.screen()
    }
}

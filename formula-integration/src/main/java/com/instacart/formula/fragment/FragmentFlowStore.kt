package com.instacart.formula.fragment

import com.instacart.formula.integration.BackStackStore
import com.instacart.formula.integration.Binding
import com.instacart.formula.integration.FlowStore
import com.instacart.formula.integration.DisposableScope
import com.instacart.formula.integration.FragmentBindingBuilder
import io.reactivex.Flowable
import io.reactivex.Observable

/**
 * A FragmentFlowStore is responsible for managing the state of multiple [FragmentContract] instances.
 */
class FragmentFlowStore(
    private val contractStore: BackStackStore<FragmentContract<*>>,
    private val store: FlowStore<FragmentContract<*>>
) {
    companion object {
        inline fun init(crossinline init: FragmentBindingBuilder<Unit>.() -> Unit): FragmentFlowStore {
            return init(Unit, init)
        }

        inline fun <Component> init(
            rootComponent: Component,
            crossinline init: FragmentBindingBuilder<Component>.() -> Unit
        ): FragmentFlowStore {
            val contractStore = BackStackStore<FragmentContract<*>>()

            val factory: (Unit) -> DisposableScope<Component> = {
                DisposableScope(component = rootComponent, onDispose = {})
            }

            val bindings = FragmentBindingBuilder.build(init)
            val root = Binding.composite(factory, bindings)
            val store = FlowStore(contractStore.stateChanges(), root)
            return FragmentFlowStore(contractStore, store)
        }
    }

    fun onLifecycleEffect(event: FragmentLifecycleEvent) {
        contractStore.onLifecycleEffect(event)
    }

    fun state(): Observable<FragmentFlowState> {
        return store.state()
    }
}

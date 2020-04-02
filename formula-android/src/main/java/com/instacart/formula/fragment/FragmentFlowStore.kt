package com.instacart.formula.fragment

import com.instacart.formula.integration.BackStackStore
import com.instacart.formula.integration.Binding
import com.instacart.formula.integration.FlowStore
import com.instacart.formula.integration.DisposableScope
import com.instacart.formula.integration.FragmentBindingBuilder
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

/**
 * A FragmentFlowStore is responsible for managing the state of multiple [FragmentContract] instances.
 */
class FragmentFlowStore(
    private val contractStore: BackStackStore<FragmentContract<*>>,
    private val store: FlowStore<FragmentContract<*>>
) {
    companion object {
        inline fun init(
            crossinline init: FragmentBindingBuilder<Unit>.() -> Unit
        ): FragmentFlowStore {
            return init(Unit, init)
        }

        inline fun <Component> init(
            rootComponent: Component,
            crossinline contracts: FragmentBindingBuilder<Component>.() -> Unit
        ): FragmentFlowStore {
            val contractStore = BackStackStore<FragmentContract<*>>()

            val factory: (Unit) -> DisposableScope<Component> = {
                DisposableScope(component = rootComponent, onDispose = {})
            }

            val bindings = FragmentBindingBuilder.build(contracts)
            val root = Binding.composite(factory, bindings)
            val store = FlowStore(contractStore.stateChanges(), root)
            return FragmentFlowStore(contractStore, store)
        }
    }

    private val visibleContractEvents = PublishRelay.create<FragmentContract<*>>()
    private val hiddenContractEvents = PublishRelay.create<FragmentContract<*>>()

    fun onLifecycleEffect(event: FragmentLifecycleEvent) {
        contractStore.onLifecycleEffect(event)
    }

    internal fun onVisibilityChanged(contract: FragmentContract<*>, visible: Boolean) {
        if (visible) {
            visibleContractEvents.accept(contract)
        } else {
            hiddenContractEvents.accept(contract)
        }
    }

    fun state(environment: FragmentEnvironment): Observable<FragmentFlowState> {
        val contractShown = visibleContractEvents.map { contract ->
            { list: List<FragmentContract<*>> ->
                if (!list.contains(contract)) {
                    list + contract
                } else {
                    // TODO: should we log this duplicate visibility event?
                    list
                }
            }
        }

        val contractHidden = hiddenContractEvents.map { contract ->
            { list: List<FragmentContract<*>> ->
                list - contract
            }
        }

        val visibleContracts = Observable.merge(contractShown, contractHidden)
            .scan(emptyList<FragmentContract<*>>()) { list, reducer -> reducer(list) }
            .distinctUntilChanged()

        return Observable.combineLatest(
            visibleContracts,
            store.state(environment),
            BiFunction { visible, state ->
                FragmentFlowState(
                    activeKeys = state.backStack.keys,
                    visibleKeys = visible,
                    states = state.states
                )
            })
    }
}

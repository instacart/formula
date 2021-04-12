package com.instacart.formula.fragment

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.rxjava3.RxStream
import com.instacart.formula.integration.Binding
import com.instacart.formula.integration.DisposableScope
import com.instacart.formula.integration.FragmentBindingBuilder
import com.instacart.formula.integration.KeyState
import com.instacart.formula.integration.LifecycleEvent
import com.instacart.formula.rxjava3.toObservable
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable

/**
 * A FragmentFlowStore is responsible for managing the state of multiple [FragmentContract] instances.
 */
class FragmentFlowStore(
    private val root: Binding<Unit>
) : Formula<FragmentEnvironment, FragmentFlowState, FragmentFlowState> {
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
            val factory: (Unit) -> DisposableScope<Component> = {
                DisposableScope(component = rootComponent, onDispose = {})
            }

            val bindings = FragmentBindingBuilder.build(contracts)
            val root = Binding.composite(factory, bindings)
            return FragmentFlowStore(root)
        }
    }


    private val lifecycleEvents = PublishRelay.create<FragmentLifecycleEvent>()
    private val visibleContractEvents = PublishRelay.create<FragmentKey>()
    private val hiddenContractEvents = PublishRelay.create<FragmentKey>()

    private val lifecycleEventStream = RxStream.fromObservable { lifecycleEvents }
    private val visibleContractEventStream = RxStream.fromObservable { visibleContractEvents }
    private val hiddenContractEventStream = RxStream.fromObservable { hiddenContractEvents }

    internal fun onLifecycleEffect(event: FragmentLifecycleEvent) {
        lifecycleEvents.accept(event)
    }

    internal fun onVisibilityChanged(contract: FragmentKey, visible: Boolean) {
        if (visible) {
            visibleContractEvents.accept(contract)
        } else {
            hiddenContractEvents.accept(contract)
        }
    }

    override fun initialState(input: FragmentEnvironment): FragmentFlowState = FragmentFlowState()

    override fun evaluate(
        input: FragmentEnvironment,
        state: FragmentFlowState,
        context: FormulaContext<FragmentFlowState>
    ): Evaluation<FragmentFlowState> {
        val rootInput = Binding.Input(
            environment = input,
            component = Unit,
            activeKeys = state.activeKeys,
            onStateChanged = context.eventCallback {
                transition(state.copy(states = state.states.plus(it.key to it)))
            }
        )
        root.bind(context, rootInput)

        return Evaluation(
            output = state,
            updates = context.updates {
                events(lifecycleEventStream) { event ->
                    val key = event.key
                    when (event) {
                        is LifecycleEvent.Removed -> {
                            val updated = state.copy(
                                activeKeys = state.activeKeys.minus(key),
                                states = state.states.minus(key)
                            )
                            transition(updated)
                        }
                        is LifecycleEvent.Added -> {
                            if (!state.activeKeys.contains(key)) {
                                // We want to emit an empty state update if key is not handled.
                                val notHandled = if (!root.binds(key)) {
                                    listOf(Pair(key, KeyState(key, "missing-registration")))
                                } else {
                                    emptyList()
                                }

                                transition(state.copy(
                                    activeKeys = state.activeKeys.plus(key),
                                    states = state.states.plus(notHandled)
                                ))
                            } else {
                                none()
                            }
                        }
                    }
                }

                events(visibleContractEventStream) {
                    if (state.visibleKeys.contains(it)) {
                        // TODO: should we log this duplicate visibility event?
                        none()
                    } else {
                        transition(state.copy(visibleKeys = state.visibleKeys.plus(it)))
                    }
                }

                events(hiddenContractEventStream) {
                    transition(state.copy(visibleKeys = state.visibleKeys.minus(it)))
                }
            }
        )
    }

    internal fun state(environment: FragmentEnvironment): Observable<FragmentFlowState> {
        return toObservable(environment)
    }
}

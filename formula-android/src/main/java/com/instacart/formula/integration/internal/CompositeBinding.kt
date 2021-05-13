package com.instacart.formula.integration.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.Stream
import com.instacart.formula.android.internal.forEachIndices
import com.instacart.formula.integration.Binding
import com.instacart.formula.android.internal.ComponentFactory
import com.instacart.formula.integration.DisposableScope

/**
 * Defines how a group of keys should be bound to their integrations.
 *
 * @param ParentComponent A component associated with the parent. Often this will map to the parent dagger component.
 * @param ScopedComponent A component that is initialized when user enters this flow and is shared between
 *                  all the screens within the flow. Component will be destroyed when user exists the flow.
 */
internal class CompositeBinding<ParentComponent, ScopedComponent>(
    private val scopeFactory: ComponentFactory<ParentComponent, ScopedComponent>,
    private val types: Set<Class<*>>,
    private val bindings: List<Binding<ScopedComponent>>
) : Binding<ParentComponent>(),
    Formula<Binding.Input<ParentComponent>, CompositeBinding.State<ScopedComponent>, Unit> {

    data class State<ScopedComponent>(
        val component: DisposableScope<ScopedComponent>? = null
    )

    override fun types(): Set<Class<*>> = types

    override fun binds(key: Any): Boolean {
        bindings.forEachIndices {
            if (it.binds(key)) return true
        }
        return false
    }

    override fun bind(context: FormulaContext<*>, input: Input<ParentComponent>) {
        context.child(this, input)
    }

    override fun key(input: Input<ParentComponent>): Any? = this

    override fun initialState(input: Input<ParentComponent>): State<ScopedComponent> {
        return State()
    }

    override fun evaluate(
        input: Input<ParentComponent>,
        state: State<ScopedComponent>,
        context: FormulaContext<State<ScopedComponent>>
    ): Evaluation<Unit> {
        val component = state.component
        if (component != null) {
            val childInput = Input(
                input.environment,
                component.component,
                input.activeKeys,
                input.onInitializeFeature,
            )
            bindings.forEachIndices {
                it.bind(context, childInput)
            }
        }
        return Evaluation(
            output = Unit,
            updates = context.updates {
                val isInScope = input.activeKeys.any { binds(it.key) }
                events(Stream.onData(isInScope)) {
                    if (isInScope && component == null) {
                        transition(State(component = scopeFactory.invoke(input.component)))
                    } else if (!isInScope && component != null) {
                        transition(State<ScopedComponent>()) {
                            component.dispose()
                        }
                    } else {
                        none()
                    }
                }

                events(Stream.onTerminate()) {
                    transition { component?.dispose() }
                }
            }
        )
    }
}

package com.instacart.formula.integration.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.fragment.FragmentKey
import com.instacart.formula.integration.Binding
import com.instacart.formula.integration.Integration
import com.instacart.formula.integration.KeyState
import com.instacart.formula.rxjava3.RxStream
import io.reactivex.rxjava3.core.Observable

/**
 * Defines how a specific key should be bound to its [Integration],
 */
internal class SingleBinding<Component, Key : FragmentKey, State : Any>(
    private val type: Class<Key>,
    private val integration: Integration<Component, Key, State>
) : Binding<Component>(), Formula<Binding.Input<Component>, Unit, Unit> {

    override fun types(): Set<Class<*>> {
        return setOf(type)
    }

    override fun binds(key: Any): Boolean {
        return type.isInstance(key)
    }

    override fun bind(context: FormulaContext<*>, input: Input<Component>) {
        context.child(this, input)
    }

    override fun key(input: Input<Component>): Any? = type

    override fun initialState(input: Input<Component>) = Unit

    override fun evaluate(
        input: Input<Component>,
        state: Unit,
        context: FormulaContext<Unit>
    ): Evaluation<Unit> {
        return Evaluation(
            output = state,
            updates = context.updates {
                input.activeKeys.forEachIndices { key ->
                    if (binds(key)) {
                        val stream = RxStream.fromObservable(key) {
                            integration.create(input.component, key as Key).onErrorResumeNext {
                                input.environment.onScreenError(key, it)
                                Observable.empty()
                            }
                        }
                        events(stream) { update ->
                            transition {
                                input.onStateChanged(KeyState(key, update))
                            }
                        }
                    }
                }
            }
        )
    }
}

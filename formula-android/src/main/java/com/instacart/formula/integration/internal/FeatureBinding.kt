package com.instacart.formula.integration.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.Stream
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.fragment.FragmentKey
import com.instacart.formula.integration.Binding
import com.instacart.formula.integration.FeatureEvent

/**
 * Defines how a specific key should be bound to its [FeatureFactory],
 */
internal class FeatureBinding<in Component, in Dependencies, in Key : FragmentKey>(
    private val type: Class<Key>,
    private val feature: FeatureFactory<Dependencies, Key>,
    private val toDependencies: (Component) -> Dependencies
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

    override fun key(input: Input<Component>): Any = type

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
                    if (binds(key.key)) {
                        Stream.onData(key).onEvent {
                            transition {
                                try {
                                    val dependencies = toDependencies(input.component)
                                    val feature = feature.initialize(dependencies, key.key as Key)
                                    input.onInitializeFeature(FeatureEvent.Init(key, feature))
                                } catch (e: Exception) {
                                    input.onInitializeFeature(FeatureEvent.Failure(key, e))
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

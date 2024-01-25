package com.instacart.formula.android.internal

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.Snapshot
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.FragmentKey
import com.instacart.formula.android.FeatureEvent

/**
 * Defines how a specific key should be bound to its [FeatureFactory],
 */
internal class FeatureBinding<in Component, in Dependencies, in Key : FragmentKey>(
    private val type: Class<Key>,
    private val feature: FeatureFactory<Dependencies, Key>,
    private val toDependencies: (Component) -> Dependencies
) : Binding<Component>() {

    private val formula = object : Formula<Input<Component>, Unit, Unit>() {
        override fun key(input: Input<Component>): Any = type

        override fun initialState(input: Input<Component>) = Unit

        override fun Snapshot<Input<Component>, Unit>.evaluate(): Evaluation<Unit> {
            return Evaluation(
                output = state,
                actions = context.actions {
                    input.activeFragments.forEachIndices { fragmentId ->
                        val key = fragmentId.key
                        if (binds(key)) {
                            Action.onData(fragmentId).onEvent {
                                transition {
                                    // TODO: should this happen on the main thread? It needs to be available to main thread
                                    try {
                                        val dependencies = toDependencies(input.component)
                                        val feature = input.environment.fragmentDelegate.initializeFeature(
                                            fragmentId = fragmentId,
                                            factory = feature,
                                            dependencies = dependencies,
                                            key = key as Key,
                                        )
                                        input.onInitializeFeature(FeatureEvent.Init(fragmentId, feature))
                                    } catch (e: Exception) {
                                        input.onInitializeFeature(FeatureEvent.Failure(fragmentId, e))
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    override fun types(): Set<Class<*>> {
        return setOf(type)
    }

    override fun binds(key: Any): Boolean {
        return type.isInstance(key)
    }

    override fun bind(context: FormulaContext<*, *>, input: Input<Component>) {
        context.child(formula, input)
    }
}

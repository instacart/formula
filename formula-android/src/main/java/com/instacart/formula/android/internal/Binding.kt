package com.instacart.formula.android.internal

import com.instacart.formula.FormulaContext
import com.instacart.formula.android.FeatureEvent
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.FragmentId

/**
 * Defines how specific keys bind to the state management associated
 */
@PublishedApi
internal abstract class Binding<in ParentComponent> {
    data class Input<out Component>(
        val environment: FragmentEnvironment,
        val component: Component,
        val activeFragments: List<FragmentId>,
        val onInitializeFeature: (FeatureEvent) -> Unit,
    )

    internal abstract fun type():  Class<*>

    /**
     * Returns true if this binding handles this [key]
     */
    internal abstract fun binds(key: Any): Boolean

    /**
     * Listens for active key changes and triggers [Input.onStateChanged] events.
     */
    internal abstract fun bind(context: FormulaContext<*, *>, input: Input<ParentComponent>)
}

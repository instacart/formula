package com.instacart.formula.android.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.FormulaContext
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.android.internal.Binding.Input

/**
 * Defines how a group of keys should be bound to their integrations.
 *
 * @param ParentComponent A component associated with the parent. Often this will map to the parent dagger component.
 * @param ScopedComponent A component that is initialized when user enters this flow and is shared between
 *                  all the screens within the flow. Component will be destroyed when user exists the flow.
 */
@PublishedApi
internal class CompositeBinding<ScopedComponent>(
    private val component: ScopedComponent,
    private val bindings: List<Binding<ScopedComponent>>
) {

    private val formula = object : StatelessFormula<Input<Unit>, Unit>() {
        override fun key(input: Input<Unit>): Any = this

        override fun Snapshot<Input<Unit>, Unit>.evaluate(): Evaluation<Unit> {
            val childInput = Input(
                environment = input.environment,
                component = component,
                activeFragments = input.activeFragments,
                onInitializeFeature = input.onInitializeFeature,
            )
            bindings.forEachIndices {
                it.bind(context, childInput)
            }
            return Evaluation(
                output = Unit,
            )
        }
    }


    fun binds(key: Any): Boolean {
        bindings.forEachIndices {
            if (it.binds(key)) return true
        }
        return false
    }

    fun bind(context: FormulaContext<*, *>, input: Input<Unit>) {
        context.child(formula, input)
    }
}

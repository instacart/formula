package com.instacart.formula.android

import com.instacart.formula.RuntimeConfig
import com.instacart.formula.android.events.FragmentLifecycleEvent
import com.instacart.formula.android.internal.Features
import com.instacart.formula.android.internal.FragmentStoreFormula
import com.instacart.formula.android.utils.MainThreadDispatcher
import com.instacart.formula.rxjava3.toObservable
import io.reactivex.rxjava3.core.Observable

/**
 * A FragmentStore is responsible for managing the state of multiple [FragmentKey] instances.
 */
class FragmentStore @PublishedApi internal constructor(
    private val formula: FragmentStoreFormula<*>,
) {
    companion object {
        val EMPTY = init {  }

        fun init(
            init: FeaturesBuilder<Unit>.() -> Unit
        ): FragmentStore {
            return init(Unit, init)
        }

        fun <Component> init(
            rootComponent: Component,
            init: FeaturesBuilder<Component>.() -> Unit
        ): FragmentStore {
            val features = FeaturesBuilder.build(init)
            return init(rootComponent, features)
        }

        fun <Component> init(
            component: Component,
            features: Features<Component>
        ): FragmentStore {
            val formula = FragmentStoreFormula(component, features.bindings)
            return FragmentStore(formula)
        }
    }

    internal fun onLifecycleEffect(event: FragmentLifecycleEvent) {
        formula.onLifecycleEffect(event)
    }

    internal fun onVisibilityChanged(contract: FragmentId, visible: Boolean) {
        formula.onVisibilityChanged(contract, visible)
    }

    internal fun state(environment: FragmentEnvironment): Observable<FragmentState> {
        val config = RuntimeConfig(
            defaultDispatcher = MainThreadDispatcher(),
        )
        return formula.toObservable(environment, config)
    }
}

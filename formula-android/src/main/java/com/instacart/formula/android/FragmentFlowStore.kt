package com.instacart.formula.android

import com.instacart.formula.RuntimeConfig
import com.instacart.formula.android.events.FragmentLifecycleEvent
import com.instacart.formula.android.internal.Features
import com.instacart.formula.android.internal.FragmentFlowStoreFormula
import com.instacart.formula.android.utils.MainThreadDispatcher
import com.instacart.formula.rxjava3.toObservable
import io.reactivex.rxjava3.core.Observable

/**
 * A FragmentFlowStore is responsible for managing the state of multiple [FragmentKey] instances.
 */
class FragmentFlowStore @PublishedApi internal constructor(
    private val formula: FragmentFlowStoreFormula<*>,
) {
    companion object {
        val EMPTY = init {  }

        inline fun init(
            crossinline init: FeaturesBuilder<Unit>.() -> Unit
        ): FragmentFlowStore {
            return init(Unit, init)
        }

        inline fun <Component> init(
            rootComponent: Component,
            crossinline init: FeaturesBuilder<Component>.() -> Unit
        ): FragmentFlowStore {
            val features = FeaturesBuilder.build(init)
            return init(rootComponent, features)
        }

        fun <Component> init(
            component: Component,
            features: Features<Component>
        ): FragmentFlowStore {
            val formula = FragmentFlowStoreFormula(component, features.bindings)
            return FragmentFlowStore(formula)
        }
    }

    internal fun onLifecycleEffect(event: FragmentLifecycleEvent) {
        formula.onLifecycleEffect(event)
    }

    internal fun onVisibilityChanged(contract: FragmentId, visible: Boolean) {
        formula.onVisibilityChanged(contract, visible)
    }

    internal fun state(environment: FragmentEnvironment): Observable<FragmentFlowState> {
        val config = RuntimeConfig(
            defaultDispatcher = MainThreadDispatcher(),
        )
        return formula.toObservable(environment, config)
    }
}

package com.instacart.formula.android

import com.instacart.formula.RuntimeConfig
import com.instacart.formula.android.events.FragmentLifecycleEvent
import com.instacart.formula.android.internal.FeatureComponent
import com.instacart.formula.android.internal.Features
import com.instacart.formula.android.internal.FragmentStoreFormula
import com.instacart.formula.android.utils.MainThreadDispatcher
import com.instacart.formula.rxjava3.toObservable
import io.reactivex.rxjava3.core.Observable

/**
 * A FragmentStore is responsible for managing the state of multiple [FragmentKey] instances.
 */
class FragmentStore @PublishedApi internal constructor(
    val environment: FragmentEnvironment,
    private val formula: FragmentStoreFormula,
    internal val onPreRenderFragmentState: ((FragmentState) -> Unit)? = null,
    private val onFragmentLifecycleEvent: ((FragmentLifecycleEvent) -> Unit)? = null,
) {

    class Builder {
        private var environment: FragmentEnvironment? = null
        private var onPreRenderFragmentState: ((FragmentState) -> Unit)? = null
        private var onFragmentLifecycleEvent: ((FragmentLifecycleEvent) -> Unit)? = null

        fun setFragmentEnvironment(environment: FragmentEnvironment) = apply {
            this.environment = environment
        }
        
        fun setOnPreRenderFragmentState(callback: ((FragmentState) -> Unit)?): Builder = apply {
            this.onPreRenderFragmentState = callback
        }

        fun setOnFragmentLifecycleEvent(callback: ((FragmentLifecycleEvent) -> Unit)?): Builder = apply {
            this.onFragmentLifecycleEvent = callback
        }
        
        fun build(
            init: FeaturesBuilder<Unit>.() -> Unit
        ): FragmentStore {
            return build(Unit, init)
        }

        fun <Component> build(
            rootComponent: Component,
            init: FeaturesBuilder<Component>.() -> Unit
        ): FragmentStore {
            val features = FeaturesBuilder.build(init)
            return build(rootComponent, features)
        }

        fun <Component> build(
            component: Component,
            features: Features<Component>
        ): FragmentStore {
            val featureComponent = FeatureComponent(component, features.bindings)
            val fragmentEnvironment = environment ?: FragmentEnvironment()
            val formula = FragmentStoreFormula(fragmentEnvironment, featureComponent)
            return FragmentStore(
                environment = fragmentEnvironment,
                formula = formula,
                onPreRenderFragmentState = onPreRenderFragmentState,
                onFragmentLifecycleEvent = onFragmentLifecycleEvent
            )
        }
    }

    companion object {
        val EMPTY = Builder().build {  }
    }

    internal fun onLifecycleEvent(event: FragmentLifecycleEvent) {
        formula.onLifecycleEffect(event)
        onFragmentLifecycleEvent?.invoke(event)
    }

    internal fun onVisibilityChanged(fragmentId: FragmentId, visible: Boolean) {
        formula.onVisibilityChanged(fragmentId, visible)
    }

    internal fun state(): Observable<FragmentState> {
        val config = RuntimeConfig(
            defaultDispatcher = MainThreadDispatcher(),
        )
        return formula.toObservable(config)
    }
}

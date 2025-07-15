package com.instacart.formula.android

import com.instacart.formula.RuntimeConfig
import com.instacart.formula.android.events.FragmentLifecycleEvent
import com.instacart.formula.android.internal.FeatureComponent
import com.instacart.formula.android.internal.Features
import com.instacart.formula.android.internal.FragmentStoreFormula
import com.instacart.formula.android.internal.getViewFactory
import com.instacart.formula.android.utils.MainThreadDispatcher
import com.instacart.formula.rxjava3.toObservable
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * A FragmentStore is responsible for managing the state of multiple [FragmentKey] instances.
 */
class FragmentStore @PublishedApi internal constructor(
    val environment: FragmentEnvironment,
    private val featureComponent: FeatureComponent<*>,
    private val formula: FragmentStoreFormula,
    internal val onPreRenderFragmentState: ((FragmentState) -> Unit)? = null,
    private val onFragmentLifecycleEvent: ((FragmentLifecycleEvent) -> Unit)? = null,
) {

    class Builder {
        private var environment: FragmentEnvironment? = null
        private var onPreRenderFragmentState: ((FragmentState) -> Unit)? = null
        private var onFragmentLifecycleEvent: ((FragmentLifecycleEvent) -> Unit)? = null
        private var asyncCoroutineDispatcher = Dispatchers.Default

        fun setFragmentEnvironment(environment: FragmentEnvironment) = apply {
            this.environment = environment
        }

        fun setAsyncCoroutineDispatcher(dispatcher: CoroutineDispatcher) = apply {
            asyncCoroutineDispatcher = dispatcher
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
            val fragmentEnvironment = environment ?: FragmentEnvironment()
            return FragmentStore(
                environment = fragmentEnvironment,
                formula = FragmentStoreFormula(
                    asyncDispatcher = asyncCoroutineDispatcher,
                    environment = fragmentEnvironment,
                ),
                featureComponent = FeatureComponent(component, features.bindings),
                onPreRenderFragmentState = onPreRenderFragmentState,
                onFragmentLifecycleEvent = onFragmentLifecycleEvent
            )
        }
    }

    companion object {
        val EMPTY = Builder().build {  }
    }

    private val features = mutableMapOf<FragmentId<*>, FeatureEvent>()

    internal fun onLifecycleEvent(event: FragmentLifecycleEvent) {
        val fragmentId = event.fragmentId
        when (event) {
            is FragmentLifecycleEvent.Added -> {
                if (!features.contains(fragmentId)) {
                    val feature = featureComponent.init(environment, fragmentId)
                    features[fragmentId] = feature

                    formula.fragmentAdded(feature)
                }
            }
            is FragmentLifecycleEvent.Removed -> {
                features.remove(fragmentId)
                formula.fragmentRemoved(fragmentId)
            }
        }

        onFragmentLifecycleEvent?.invoke(event)
    }

    internal fun onVisibilityChanged(fragmentId: FragmentId<*>, visible: Boolean) {
        if (visible) {
            formula.fragmentVisible(fragmentId)
        } else {
            formula.fragmentHidden(fragmentId)
        }
    }

    internal fun state(): Observable<FragmentState> {
        val config = RuntimeConfig(
            defaultDispatcher = MainThreadDispatcher(),
        )
        return formula.toObservable(config)
    }

    internal fun getViewFactory(fragmentId: FragmentId<*>): ViewFactory<Any>? {
        return features.getViewFactory(environment, fragmentId)
    }
}

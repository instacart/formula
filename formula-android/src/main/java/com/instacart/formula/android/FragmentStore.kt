package com.instacart.formula.android

import com.instacart.formula.RuntimeConfig
import com.instacart.formula.android.events.FragmentLifecycleEvent
import com.instacart.formula.android.internal.FeatureComponent
import com.instacart.formula.android.internal.Features
import com.instacart.formula.android.internal.FragmentStoreFormula
import com.instacart.formula.android.utils.MainThreadDispatcher
import com.instacart.formula.rxjava3.toObservable
import io.reactivex.rxjava3.core.Observable
import java.lang.IllegalStateException

/**
 * A FragmentStore is responsible for managing the state of multiple [FragmentKey] instances.
 */
class FragmentStore @PublishedApi internal constructor(
    private val featureComponent: FeatureComponent<*>,
    private val formula: FragmentStoreFormula,
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
            val featureComponent = FeatureComponent(component, features.bindings)
            val formula = FragmentStoreFormula()
            return FragmentStore(featureComponent, formula)
        }
    }

    private lateinit var environment: FragmentEnvironment
    private val features = mutableMapOf<FragmentId, FeatureEvent>()

    internal fun onLifecycleEffect(event: FragmentLifecycleEvent) {
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
    }

    internal fun onVisibilityChanged(fragmentId: FragmentId, visible: Boolean) {
        if (visible) {
            formula.fragmentVisible(fragmentId)
        } else {
            formula.fragmentHidden(fragmentId)
        }
    }

    internal fun state(environment: FragmentEnvironment): Observable<FragmentState> {
        this.environment = environment

        val config = RuntimeConfig(
            defaultDispatcher = MainThreadDispatcher(),
        )
        return formula.toObservable(environment, config)
    }

    internal fun getViewFactory(fragmentId: FragmentId): ViewFactory<Any>? {
        return try {
            findViewFactoryOrThrow(fragmentId)
        } catch (e: Throwable) {
            environment.onScreenError(fragmentId.key, e)
            null
        }
    }

    private fun findViewFactoryOrThrow(fragmentId: FragmentId): ViewFactory<Any> {
        val key = fragmentId.key
        val featureEvent = features[fragmentId] ?: throw IllegalStateException("Could not find feature for $key.")
        return when (featureEvent) {
            is FeatureEvent.MissingBinding -> {
                throw IllegalStateException("Missing feature factory or integration for $key. Please check your FragmentStore configuration.")
            }
            is FeatureEvent.Failure -> {
                throw IllegalStateException("Feature failed to initialize: $key", featureEvent.error)
            }
            is FeatureEvent.Init -> {
                featureEvent.feature.viewFactory
            }
        }
    }
}

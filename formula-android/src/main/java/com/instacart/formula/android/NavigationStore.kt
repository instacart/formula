package com.instacart.formula.android

import com.instacart.formula.RuntimeConfig
import com.instacart.formula.android.events.RouteLifecycleEvent
import com.instacart.formula.android.internal.FeatureComponent
import com.instacart.formula.android.internal.Features
import com.instacart.formula.android.internal.NavigationStoreFormula
import com.instacart.formula.android.internal.getViewFactory
import com.instacart.formula.android.utils.MainThreadDispatcher
import com.instacart.formula.rxjava3.toObservable
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * A NavigationStore is responsible for managing the state of multiple [RouteKey] instances.
 */
class NavigationStore @PublishedApi internal constructor(
    val environment: RouteEnvironment,
    private val featureComponent: FeatureComponent<*>,
    private val formula: NavigationStoreFormula,
    internal val onPreRenderNavigationState: ((NavigationState) -> Unit)? = null,
    private val onRouteLifecycleEvent: ((RouteLifecycleEvent) -> Unit)? = null,
) {

    class Builder {
        private var environment: RouteEnvironment? = null
        private var onPreRenderNavigationState: ((NavigationState) -> Unit)? = null
        private var onRouteLifecycleEvent: ((RouteLifecycleEvent) -> Unit)? = null
        private var asyncCoroutineDispatcher = Dispatchers.Default

        fun setRouteEnvironment(environment: RouteEnvironment) = apply {
            this.environment = environment
        }

        fun setAsyncCoroutineDispatcher(dispatcher: CoroutineDispatcher) = apply {
            asyncCoroutineDispatcher = dispatcher
        }
        
        fun setOnPreRenderNavigationState(callback: ((NavigationState) -> Unit)?): Builder = apply {
            this.onPreRenderNavigationState = callback
        }

        fun setOnRouteLifecycleEvent(callback: ((RouteLifecycleEvent) -> Unit)?): Builder = apply {
            this.onRouteLifecycleEvent = callback
        }
        
        fun build(
            init: FeaturesBuilder<Unit>.() -> Unit
        ): NavigationStore {
            return build(Unit, init)
        }

        fun <Component> build(
            rootComponent: Component,
            init: FeaturesBuilder<Component>.() -> Unit
        ): NavigationStore {
            val features = FeaturesBuilder.build(init)
            return build(rootComponent, features)
        }

        fun <Component> build(
            component: Component,
            features: Features<Component>
        ): NavigationStore {
            val routeEnvironment = environment ?: RouteEnvironment()
            return NavigationStore(
                environment = routeEnvironment,
                formula = NavigationStoreFormula(
                    asyncDispatcher = asyncCoroutineDispatcher,
                    environment = routeEnvironment,
                ),
                featureComponent = FeatureComponent(component, features.bindings),
                onPreRenderNavigationState = onPreRenderNavigationState,
                onRouteLifecycleEvent = onRouteLifecycleEvent
            )
        }
    }

    companion object {
        val EMPTY = Builder().build {  }
    }

    private val features = mutableMapOf<RouteId<*>, FeatureEvent>()

    internal fun onLifecycleEvent(event: RouteLifecycleEvent) {
        val routeId = event.routeId
        when (event) {
            is RouteLifecycleEvent.Added -> {
                if (!features.contains(routeId)) {
                    val feature = featureComponent.init(environment, routeId)
                    features[routeId] = feature

                    formula.routeAdded(feature)
                }
            }
            is RouteLifecycleEvent.Removed -> {
                features.remove(routeId)
                formula.routeRemoved(routeId)
            }
        }

        onRouteLifecycleEvent?.invoke(event)
    }

    internal fun onVisibilityChanged(routeId: RouteId<*>, visible: Boolean) {
        if (visible) {
            formula.routeVisible(routeId)
        } else {
            formula.routeHidden(routeId)
        }
    }

    internal fun state(): Observable<NavigationState> {
        val config = RuntimeConfig(
            defaultDispatcher = MainThreadDispatcher(),
        )
        return formula.toObservable(config)
    }

    internal fun getViewFactory(routeId: RouteId<*>): ViewFactory<Any>? {
        return features.getViewFactory(environment, routeId)
    }
}

package com.instacart.formula.android

import androidx.annotation.MainThread
import com.instacart.formula.android.events.RouteLifecycleEvent
import com.instacart.formula.android.internal.FeatureComponent
import com.instacart.formula.android.internal.Features
import com.instacart.formula.android.internal.NavigationStoreFormula
import kotlinx.coroutines.flow.StateFlow

/**
 * A NavigationStore is responsible for managing the state of multiple [RouteKey] instances.
 */
interface NavigationStore {

    class Builder {
        private var environment: RouteEnvironment? = null
        private var onPreRenderNavigationState: ((NavigationState) -> Unit)? = null
        private var onRouteLifecycleEvent: ((RouteLifecycleEvent) -> Unit)? = null

        fun setRouteEnvironment(environment: RouteEnvironment) = apply {
            this.environment = environment
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
            return NavigationStoreImpl(
                environment = routeEnvironment,
                formula = NavigationStoreFormula(
                    environment = routeEnvironment,
                ),
                featureComponent = FeatureComponent(component, features.bindings),
                onPreRenderNavigationState = onPreRenderNavigationState,
                onRouteLifecycleEvent = onRouteLifecycleEvent
            )
        }
    }

    val environment: RouteEnvironment
    val stateFlow: StateFlow<NavigationState>

    @MainThread
    fun onLifecycleEvent(event: RouteLifecycleEvent)

    @MainThread
    fun onVisibilityChanged(routeId: RouteId<*>, visible: Boolean)

    @MainThread
    fun getViewFactory(routeId: RouteId<*>): ViewFactory<Any>?

    fun dispose()
}

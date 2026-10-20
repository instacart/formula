package com.instacart.formula.android

import androidx.annotation.MainThread
import com.instacart.formula.RuntimeConfig
import com.instacart.formula.android.events.RouteLifecycleEvent
import com.instacart.formula.android.internal.FeatureComponent
import com.instacart.formula.android.internal.NavigationStoreFormula
import com.instacart.formula.android.internal.getViewFactory
import com.instacart.formula.android.utils.MainThreadDispatcher
import com.instacart.formula.toFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A NavigationStore is responsible for managing the state of multiple [RouteKey] instances.
 */
class NavigationStoreImpl @PublishedApi internal constructor(
    override val environment: RouteEnvironment,
    private val featureComponent: FeatureComponent<*>,
    private val formula: NavigationStoreFormula,
    private val onPreRenderNavigationState: ((NavigationState) -> Unit)? = null,
    private val onRouteLifecycleEvent: ((RouteLifecycleEvent) -> Unit)? = null,
) : NavigationStore {

    private val scope = CoroutineScope(EmptyCoroutineContext)
    private val features = mutableMapOf<RouteId<*>, FeatureEvent>()
    private val _stateFlow = MutableStateFlow(NavigationState())
    override val stateFlow: StateFlow<NavigationState> = _stateFlow.asStateFlow()

    init {
        val config = RuntimeConfig(
            defaultDispatcher = MainThreadDispatcher(),
        )

        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            withContext(Dispatchers.Unconfined) {
                formula.toFlow(config).collect { newState ->
                    onPreRenderNavigationState?.invoke(newState)
                    _stateFlow.value = newState
                }
            }
        }
    }

    @MainThread
    override fun onLifecycleEvent(event: RouteLifecycleEvent) {
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

    @MainThread
    override fun onVisibilityChanged(routeId: RouteId<*>, visible: Boolean) {
        if (visible) {
            formula.routeVisible(routeId)
        } else {
            formula.routeHidden(routeId)
        }
    }

    @MainThread
    override fun getViewFactory(routeId: RouteId<*>): ViewFactory<Any>? {
        return features.getViewFactory(environment, routeId)
    }

    override fun dispose() {
        scope.cancel()
    }
}

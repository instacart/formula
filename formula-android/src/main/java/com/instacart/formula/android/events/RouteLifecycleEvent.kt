package com.instacart.formula.android.events

import com.instacart.formula.android.RouteId

/**
 * Models when a route is added to or removed from the host's navigation state, indicating
 * when to initialize and dispose its feature state.
 */
sealed class RouteLifecycleEvent {

    abstract val routeId: RouteId<*>

    data class Added(
        override val routeId: RouteId<*>
    ) : RouteLifecycleEvent()

    data class Removed(
        override val routeId: RouteId<*>,
        val lastState: Any? = null
    ) : RouteLifecycleEvent()
}

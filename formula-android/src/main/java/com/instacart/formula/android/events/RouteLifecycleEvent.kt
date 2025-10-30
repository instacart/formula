package com.instacart.formula.android.events

import com.instacart.formula.android.RouteId

/**
 * Models when a [RouteId] is attached and detached. Provides a way to indicate
 * when to initialize state stream and when to destroy it.
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

package com.instacart.formula.android.fakes

import com.instacart.formula.android.RouteId
import com.instacart.formula.android.RouteKey
import com.instacart.formula.android.events.RouteLifecycleEvent

fun RouteKey.asAddedEvent(instanceId: String = "") = RouteLifecycleEvent.Added(
    routeId = RouteId(instanceId, this)
)
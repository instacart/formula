package com.instacart.formula.android

/**
 * Identifies a route instance by combining its key and host-provided unique instance id.
 *
 * @param instanceId Unique identifier used to distinguish route instances with the same [key].
 * @param key Route key used to create this route.
 */
data class RouteId<out Type : RouteKey>(
    val instanceId: String,
    val key: Type,
)

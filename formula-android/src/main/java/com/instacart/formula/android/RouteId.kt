package com.instacart.formula.android

/**
 * An object used to identify a route. It combines both a user generated [key] and
 * a generated [String] id.
 *
 * @param instanceId Unique identifier used to distinguish a route. Since there
 * can be multiple routes with the same [key], we use this as an extra identifier.
 *
 * @param key Route key used to create this route.
 */
data class RouteId<out Type : RouteKey>(
    val instanceId: String,
    val key: Type,
)

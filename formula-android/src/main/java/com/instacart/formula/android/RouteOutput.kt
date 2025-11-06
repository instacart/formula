package com.instacart.formula.android

/**
 * Defines the current render model for a specific [key].
 */
data class RouteOutput(val key: RouteKey, val renderModel: Any)

@Deprecated(
    message = "FragmentOutput has been renamed to RouteOutput",
    replaceWith = ReplaceWith("RouteOutput", "com.instacart.formula.android.RouteOutput")
)
typealias FragmentOutput = RouteOutput
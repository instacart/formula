package com.instacart.formula.android

import androidx.fragment.app.Fragment
import com.instacart.formula.android.internal.getFragmentInstanceId
import com.instacart.formula.android.internal.getRouteKey

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

/**
 * Gets a [RouteId] for a given [Fragment].
 */
fun Fragment.getFormulaRouteId(): RouteId<*> {
    return RouteId(
        instanceId = getFragmentInstanceId(),
        key = this@getFormulaRouteId.getRouteKey()
    )
}

@Deprecated(
    message = "FragmentId has been renamed to RouteId",
    replaceWith = ReplaceWith("RouteId", "com.instacart.formula.android.RouteId")
)
typealias FragmentId<Type> = RouteId<Type>

@Deprecated(
    message = "getFormulaFragmentId has been renamed to getFormulaRouteId",
    replaceWith = ReplaceWith("getFormulaRouteId()", "com.instacart.formula.android.getFormulaRouteId")
)
fun Fragment.getFormulaFragmentId(): RouteId<*> = getFormulaRouteId()
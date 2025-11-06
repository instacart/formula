package com.instacart.formula.android.internal

import com.instacart.formula.android.FeatureEvent
import com.instacart.formula.android.RouteEnvironment
import com.instacart.formula.android.RouteId
import com.instacart.formula.android.ViewFactory
import java.lang.IllegalStateException

/** Functionally the same as [Iterable.forEach] except it generates an index-based loop that doesn't use an [Iterator]. */
internal inline fun <T> List<T>.forEachIndices(action: (T) -> Unit) {
    for (i in indices) {
        action(get(i))
    }
}

internal fun Map<RouteId<*>, FeatureEvent>.getViewFactory(environment: RouteEnvironment, routeId: RouteId<*>): ViewFactory<Any>? {
    return try {
        findViewFactoryOrThrow(routeId)
    } catch (e: Throwable) {
        environment.onScreenError(routeId.key, e)
        null
    }
}

private fun Map<RouteId<*>, FeatureEvent>.findViewFactoryOrThrow(
    routeId: RouteId<*>
): ViewFactory<Any> {
    val key = routeId.key
    val featureEvent = this[routeId] ?: throw IllegalStateException("Could not find feature for $key.")
    return when (featureEvent) {
        is FeatureEvent.MissingBinding -> {
            throw IllegalStateException("Missing feature factory or integration for $key. Please check your FragmentStore configuration.")
        }
        is FeatureEvent.Failure -> {
            throw IllegalStateException("Feature failed to initialize: $key", featureEvent.error)
        }
        is FeatureEvent.Init -> {
            featureEvent.feature.viewFactory
        }
    }
}
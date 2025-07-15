package com.instacart.formula.android.internal

import com.instacart.formula.android.FeatureEvent
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.FragmentId
import com.instacart.formula.android.ViewFactory
import java.lang.IllegalStateException

/** Functionally the same as [Iterable.forEach] except it generates an index-based loop that doesn't use an [Iterator]. */
internal inline fun <T> List<T>.forEachIndices(action: (T) -> Unit) {
    for (i in indices) {
        action(get(i))
    }
}

internal fun Map<FragmentId<*>, FeatureEvent>.getViewFactory(environment: FragmentEnvironment, fragmentId: FragmentId<*>): ViewFactory<Any>? {
    return try {
        findViewFactoryOrThrow(fragmentId)
    } catch (e: Throwable) {
        environment.onScreenError(fragmentId.key, e)
        null
    }
}

private fun Map<FragmentId<*>, FeatureEvent>.findViewFactoryOrThrow(
    fragmentId: FragmentId<*>
): ViewFactory<Any> {
    val key = fragmentId.key
    val featureEvent = this[fragmentId] ?: throw IllegalStateException("Could not find feature for $key.")
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
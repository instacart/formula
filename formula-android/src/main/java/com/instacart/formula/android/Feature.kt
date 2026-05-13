package com.instacart.formula.android

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Feature is based on uni-directional state management where a single state model drives
 * the view rendering logic. It is used to support [FormulaFragment] by providing [ViewFactory]
 * and the state [factory] that creates a [StateFlow].
 *
 * To define a feature, we need to create a [FeatureFactory] for a specific [RouteKey] type
 * and [bind][FeaturesBuilder.bind] it to the [NavigationStore].
 *
 * Take a look at [FeatureFactory] for more information.
 */
class Feature private constructor(
    val initAsync: Boolean,
    val factory: (CoroutineScope) -> StateFlow<Any>,
    val viewFactory: ViewFactory<Any>,
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        operator fun <RenderModel : Any> invoke(
            viewFactory: ViewFactory<RenderModel>,
            initAsync: Boolean = false,
            stateProvider: (CoroutineScope) -> StateFlow<RenderModel>,
        ): Feature {
            return Feature(
                initAsync = initAsync,
                factory = stateProvider as (CoroutineScope) -> StateFlow<Any>,
                viewFactory = viewFactory as ViewFactory<Any>,
            )
        }
    }
}

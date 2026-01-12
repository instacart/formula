package com.instacart.formula.android

import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Feature is based on uni-directional state management where a single state model drives
 * the view rendering logic. It is used to support [FormulaFragment] and Nav3 navigation
 * by providing a [RenderFactory] and state observable.
 *
 * To define a feature, we need to create a [FeatureFactory] for a specific [RouteKey] type
 * and [bind][FeaturesBuilder.bind] it to the [NavigationStore].
 *
 * Take a look at [FeatureFactory] for more information.
 */
sealed class Feature {
    companion object {
        /**
         * Creates a Feature with RxJava Observable state.
         */
        @Suppress("UNCHECKED_CAST")
        operator fun <RenderModel : Any> invoke(
            state: Observable<RenderModel>,
            renderFactory: RenderFactory<RenderModel>
        ): Feature {
            return RxJavaFeature(
                stateObservable = state as Observable<Any>,
                renderFactory = renderFactory as RenderFactory<Any>,
            )
        }

        /**
         * Creates a Feature with StateFlow state.
         */
        @Suppress("UNCHECKED_CAST")
        operator fun <RenderModel : Any> invoke(
            renderFactory: RenderFactory<RenderModel>,
            initAsync: Boolean = false,
            stateProvider: (CoroutineScope) -> StateFlow<RenderModel>,
        ): Feature {
            return StateFlowFeature(
                initAsync = initAsync,
                factory = stateProvider as (CoroutineScope) -> StateFlow<Any>,
                renderFactory = renderFactory as RenderFactory<Any>,
            )
        }
    }

    /**
     * The factory used to render the feature's state.
     */
    abstract val renderFactory: RenderFactory<Any>
}

internal class RxJavaFeature(
    val stateObservable: Observable<Any>,
    override val renderFactory: RenderFactory<Any>
) : Feature()

internal class StateFlowFeature(
    val initAsync: Boolean = false,
    val factory: (CoroutineScope) -> StateFlow<Any>,
    override val renderFactory: RenderFactory<Any>,
) : Feature()

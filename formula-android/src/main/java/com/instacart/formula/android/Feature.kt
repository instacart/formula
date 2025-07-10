package com.instacart.formula.android

import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Feature is based on uni-directional state management where a single state model drives
 * the view rendering logic. It is used to support [FormulaFragment] by providing [ViewFactory]
 * and the [stateObservable] observable.
 *
 * To define a feature, we need to create a [FeatureFactory] for a specific [FragmentKey] type
 * and [bind][FeaturesBuilder.bind] it to the [FragmentStore].
 *
 * Take a look at [FeatureFactory] for more information.
 */
sealed class Feature {
    companion object {
        @Suppress("UNCHECKED_CAST")
        operator fun <RenderModel : Any> invoke(
            state: Observable<RenderModel>,
            viewFactory: ViewFactory<RenderModel>
        ): Feature {
            return RxJavaFeature(
                stateObservable = state as Observable<Any>,
                viewFactory = viewFactory as ViewFactory<Any>,
            )
        }

        @Suppress("UNCHECKED_CAST")
        operator fun <RenderModel : Any> invoke(
            viewFactory: ViewFactory<RenderModel>,
            initAsync: Boolean = false,
            stateProvider: (CoroutineScope) -> StateFlow<RenderModel>,
        ): Feature {
            return StateFlowFeature(
                initAsync = initAsync,
                factory = stateProvider as (CoroutineScope) -> StateFlow<Any>,
                viewFactory = viewFactory as ViewFactory<Any>,
            )
        }
    }

    abstract val viewFactory: ViewFactory<Any>
}

internal class RxJavaFeature(
    val stateObservable: Observable<Any>,
    override val viewFactory: ViewFactory<Any>
) : Feature()

internal class StateFlowFeature(
    val initAsync: Boolean = false,
    val factory: (CoroutineScope) -> StateFlow<Any>,
    override val viewFactory: ViewFactory<Any>,
): Feature()
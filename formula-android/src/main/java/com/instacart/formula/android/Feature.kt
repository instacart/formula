package com.instacart.formula.android

import io.reactivex.rxjava3.core.Observable

/**
 * Feature is based on uni-directional state management where a single state model drives
 * the view rendering logic. It is used to support [FormulaFragment] by providing [ViewFactory]
 * and the [stateObservable] observable.
 *
 * To define a feature, we need to create a [FeatureFactory] for a specific [FragmentKey] type
 * and [bind][FragmentStoreBuilder.bind] it to the [FragmentFlowStore].
 *
 * Take a look at [FeatureFactory] for more information.
 */
class Feature private constructor(
    val stateObservable: Observable<Any>,
    val viewFactory: ViewFactory<Any>
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        operator fun <RenderModel : Any> invoke(
            state: Observable<RenderModel>,
            viewFactory: ViewFactory<RenderModel>
        ): Feature {
            return Feature(
                stateObservable = state as Observable<Any>,
                viewFactory = viewFactory as ViewFactory<Any>,
            )
        }
    }
}
package com.instacart.formula.android

import io.reactivex.rxjava3.core.Observable
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.fragment.FragmentFlowStore

/**
 * Feature is based on uni-directional state management where a single state model drives
 * the view rendering logic. It is used to support [FormulaFragment] by providing [ViewFactory]
 * and the [state] observable.
 *
 * To define a feature, we need to create a [FeatureFactory] for a specific [FragmentKey] type
 * and [bind][FragmentBindingBuilder.bind] it to the [FragmentFlowStore].
 *
 * Take a look at [FeatureFactory] for more information.
 */
class Feature<RenderModel>(
    val state: Observable<RenderModel>,
    val viewFactory: ViewFactory<RenderModel>
)
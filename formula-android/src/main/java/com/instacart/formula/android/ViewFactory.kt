package com.instacart.formula.android

import android.content.Context

/**
 * View factory is used by [FormulaFragment] (and Compose-native hosts) to create a [FeatureView]
 * describing how a route's render model is rendered.
 *
 * The typical implementation extends [ComposeViewFactory].
 */
fun interface ViewFactory<RenderModel> {

    class Params(
        val context: Context,
    )

    /**
     * Returns a [FeatureView] describing how to render the route. May be invoked from
     * [FormulaFragment.onCreateView] or directly by a Compose-native host.
     */
    fun create(params: Params): FeatureView<RenderModel>
}

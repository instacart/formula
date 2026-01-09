package com.instacart.formula.android

import android.view.View

/**
 * Interface representing a feature's view layer.
 *
 * Two implementations exist:
 * - [ViewFeatureView] - For Fragment-based rendering with Android Views
 * - TODO - Nav3-based navigation
 *
 * Use [ViewFactory] to create [ViewFeatureView] instances for Fragments.
 * Use [ComposeViewFactory][com.instacart.formula.android.compose.ComposeViewFactory]
 * to create either type depending on the rendering context.
 */
interface FeatureView<RenderModel> {
    /**
     * Function called to apply the render model to the view/composable.
     */
    val setOutput: (RenderModel) -> Unit
}

/**
 * Feature view for Fragment-based rendering.
 *
 * Provides [FormulaFragment] with the root Android view which will be returned as
 * part of [FormulaFragment.onCreateView] and the logic to bind the state observable to the
 * rendering. Formula fragment uses [ViewFactory.create] to instantiate [ViewFeatureView].
 *
 * Use [ViewFactory.fromLayout] and [LayoutViewFactory] to define a [ViewFactory] which can create
 * [ViewFeatureView].
 *
 * @param view The root Android view.
 * @param setOutput A function called to apply [RenderModel] to the view.
 * @param lifecycleCallbacks Optional lifecycle callbacks if you need to know the Fragment state.
 */
class ViewFeatureView<RenderModel>(
    val view: View,
    override val setOutput: (RenderModel) -> Unit,
    val lifecycleCallbacks: FragmentLifecycleCallback? = null,
) : FeatureView<RenderModel>

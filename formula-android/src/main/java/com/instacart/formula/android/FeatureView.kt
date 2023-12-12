package com.instacart.formula.android

import android.view.View
import com.instacart.formula.Cancelable
import io.reactivex.rxjava3.core.Observable

/**
 * Feature view provides [FormulaFragment] with the root Android view which will be returned as
 * part of [FormulaFragment.onCreateView] and the logic to bind the state observable to the
 * rendering. Formula fragment uses [ViewFactory.create] to instantiate [FeatureView].
 *
 * Use [ViewFactory.fromLayout] and [LayoutViewFactory] to define a [ViewFactory] which can create
 * [FeatureView].
 *
 * @param view The root Android view.
 * @param setOutput A function called to apply [RenderModel] to the view.
 * @param lifecycleCallbacks Optional lifecycle callbacks if you need to know the Fragment state.
 */
class FeatureView<RenderModel>(
    val view: View,
    val setOutput: (RenderModel) -> Unit,
    val lifecycleCallbacks: FragmentLifecycleCallback? = null,
)

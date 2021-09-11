package com.instacart.formula.android

import android.view.View
import androidx.lifecycle.LifecycleObserver
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
 * @param bind A bind function connects state observable to the view rendering.
 * @param lifecycleObserver Optional lifecycle callbacks if you need to know the Fragment state.
 */
class FeatureView<RenderModel>(
    val view: View,
    val bind: (State<RenderModel>) -> Cancelable?,
    val lifecycleCallbacks: FragmentLifecycleCallback? = null,
) {
    class State<RenderModel>(
        val observable: Observable<RenderModel>,
        val onError: (Throwable) -> Unit,
    )
}
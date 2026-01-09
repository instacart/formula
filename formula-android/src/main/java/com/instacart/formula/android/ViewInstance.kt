package com.instacart.formula.android

import android.view.View
import com.instacart.formula.RenderView
import com.instacart.formula.Renderer

/**
 * View instance contains an initialized Android [view] and provides factory
 * [methods][featureView] to create a [ViewFeatureView].
 */
abstract class ViewInstance {
    /**
     * Android view which will be used to create [ViewFeatureView]. This view will be returned
     * as part of [FormulaFragment.onCreateView].
     */
    abstract val view: View

    /**
     * Creates a [ViewFeatureView] from a [render] function
     */
    fun <RenderModel> featureView(
        lifecycleCallbacks: FragmentLifecycleCallback? = null,
        render: (RenderModel) -> Unit
    ): ViewFeatureView<RenderModel> {
        return ViewFeatureView(
            view = view,
            setOutput = Renderer.create(render),
            lifecycleCallbacks = lifecycleCallbacks
        )
    }

    /**
     * Creates a [ViewFeatureView] from a [RenderView].
     */
    fun <RenderModel> featureView(
        renderView: RenderView<RenderModel>,
        lifecycleCallbacks: FragmentLifecycleCallback? = null,
    ): ViewFeatureView<RenderModel> {
        return ViewFeatureView(
            view = view,
            setOutput = renderView.render,
            lifecycleCallbacks = lifecycleCallbacks,
        )
    }
}
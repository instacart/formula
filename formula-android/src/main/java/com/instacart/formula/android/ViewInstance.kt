package com.instacart.formula.android

import android.view.View
import com.instacart.formula.RenderView
import com.instacart.formula.Renderer

/**
 * View instance contains an initialized Android [view] and provides factory
 * [methods][featureView] to create a [FeatureView].
 */
abstract class ViewInstance {
    /**
     * Android view which will be used to create [FeatureView]. This view will be returned
     * as part of [FormulaFragment.onCreateView].
     */
    abstract val view: View

    /**
     * Creates a [FeatureView] from a [render] function
     */
    fun <RenderModel> featureView(
        render: (RenderModel) -> Unit
    ): FeatureView<RenderModel> {
        return FeatureView(
            view = view,
            setOutput = Renderer.create(render),
        )
    }

    /**
     * Creates a [FeatureView] from a [RenderView].
     */
    fun <RenderModel> featureView(
        renderView: RenderView<RenderModel>,
    ): FeatureView<RenderModel> {
        return FeatureView(
            view = view,
            setOutput = renderView.render,
        )
    }
}
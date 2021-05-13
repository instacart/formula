package com.instacart.formula.android

import android.view.View
import com.instacart.formula.RenderView
import com.instacart.formula.Renderer
import com.instacart.formula.android.views.FeatureViewBindFunction
import com.instacart.formula.fragment.FragmentLifecycleCallback

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
        lifecycleCallbacks: FragmentLifecycleCallback? = null,
        render: (RenderModel) -> Unit
    ): FeatureView<RenderModel> {
        return featureView(
            renderer = Renderer.create(render),
            lifecycleCallbacks = lifecycleCallbacks
        )
    }
    /**
     * Creates a [FeatureView] from a [Renderer].
     */
    fun <RenderModel> featureView(
        renderer: Renderer<RenderModel>,
        lifecycleCallbacks: FragmentLifecycleCallback? = null,
    ): FeatureView<RenderModel> {
        return FeatureView(
            view = view,
            bind = FeatureViewBindFunction(renderer),
            lifecycleCallbacks = lifecycleCallbacks
        )
    }

    /**
     * Creates a [FeatureView] from a [RenderView].
     */
    fun <RenderModel> featureView(
        renderView: RenderView<RenderModel>,
        lifecycleCallbacks: FragmentLifecycleCallback? = null,
    ): FeatureView<RenderModel> {
        return featureView(
            renderer = renderView.render,
            lifecycleCallbacks = lifecycleCallbacks,
        )
    }
}
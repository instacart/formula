package com.instacart.formula.android.views

import androidx.annotation.LayoutRes
import com.instacart.formula.android.FeatureView
import com.instacart.formula.android.LayoutViewFactory
import com.instacart.formula.android.ViewInstance

/**
 * An implementation of [LayoutViewFactory] which delegates [FeatureView] creation to [createView].
 */
internal class DelegateLayoutViewFactory<RenderModel : Any>(
    @LayoutRes layoutId: Int,
    private val createView: ViewInstance.() -> FeatureView<RenderModel>
) : LayoutViewFactory<RenderModel>(layoutId) {

    override fun ViewInstance.create(): FeatureView<RenderModel> {
        return createView()
    }
}
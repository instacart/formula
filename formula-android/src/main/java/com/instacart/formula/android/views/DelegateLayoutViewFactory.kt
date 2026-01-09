package com.instacart.formula.android.views

import androidx.annotation.LayoutRes
import com.instacart.formula.android.ViewFeatureView
import com.instacart.formula.android.LayoutViewFactory
import com.instacart.formula.android.ViewInstance

/**
 * An implementation of [LayoutViewFactory] which delegates [ViewFeatureView] creation to [createView].
 */
internal class DelegateLayoutViewFactory<RenderModel>(
    @LayoutRes layoutId: Int,
    private val createView: ViewInstance.() -> ViewFeatureView<RenderModel>
) : LayoutViewFactory<RenderModel>(layoutId) {

    override fun ViewInstance.create(): ViewFeatureView<RenderModel> {
        return createView()
    }
}
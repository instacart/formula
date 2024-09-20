package com.instacart.testutils.android

import android.view.View
import com.instacart.formula.android.FeatureView
import com.instacart.formula.android.FragmentLifecycleCallback
import com.instacart.formula.android.LayoutViewFactory
import com.instacart.formula.android.ViewInstance

class TestViewFactory<RenderModel>(
    private val fragmentLifecycleCallbacks: FragmentLifecycleCallback? = null,
    private val render: (View, RenderModel) -> Unit = { _, _ -> },
) : LayoutViewFactory<RenderModel>(R.layout.test_fragment_layout) {
    override fun ViewInstance.create(): FeatureView<RenderModel> {
        return featureView(fragmentLifecycleCallbacks) {
            render(view, it)
        }
    }
}
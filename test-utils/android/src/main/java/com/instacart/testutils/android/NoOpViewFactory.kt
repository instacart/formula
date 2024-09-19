package com.instacart.testutils.android

import com.instacart.formula.android.FeatureView
import com.instacart.formula.android.LayoutViewFactory
import com.instacart.formula.android.ViewInstance

class NoOpViewFactory<RenderModel> : LayoutViewFactory<RenderModel>(R.layout.test_fragment_layout) {
    override fun ViewInstance.create(): FeatureView<RenderModel> {
        return featureView {  }
    }
}
package com.instacart.formula.android.fakes

import com.instacart.formula.android.FeatureView
import com.instacart.formula.android.LayoutViewFactory
import com.instacart.formula.android.ViewInstance

class NoOpViewFactory<RenderModel> : LayoutViewFactory<RenderModel>(-1) {
    override fun ViewInstance.create(): FeatureView<RenderModel> {
        return featureView {  }
    }
}
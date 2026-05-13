package com.instacart.testutils.android

import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.RouteKey
import com.instacart.formula.android.ViewFactory
import kotlinx.coroutines.flow.MutableStateFlow

class NoOpFeatureFactory<FragmentKeyT : RouteKey>(
    private val viewFactory: ViewFactory<Unit> = TestViewFactory(),
) : FeatureFactory<Unit, FragmentKeyT>() {
    override fun Params.initialize(): Feature {
        return Feature(
            viewFactory = viewFactory,
        ) {
            MutableStateFlow(Unit)
        }
    }
}

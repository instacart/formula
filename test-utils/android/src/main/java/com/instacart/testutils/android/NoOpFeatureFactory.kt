package com.instacart.testutils.android

import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.RouteKey
import com.instacart.formula.android.ViewFactory
import io.reactivex.rxjava3.core.Observable

class NoOpFeatureFactory<FragmentKeyT : RouteKey>(
    private val viewFactory: ViewFactory<FragmentKeyT> = TestViewFactory(),
) : FeatureFactory<Unit, FragmentKeyT>() {
    override fun Params.initialize(): Feature {
        return Feature(
            state = Observable.empty(),
            viewFactory = viewFactory,
        )
    }
}
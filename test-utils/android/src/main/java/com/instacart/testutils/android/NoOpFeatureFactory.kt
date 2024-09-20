package com.instacart.testutils.android

import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.FragmentKey
import com.instacart.formula.android.ViewFactory
import io.reactivex.rxjava3.core.Observable

class NoOpFeatureFactory<FragmentKeyT : FragmentKey>(
    private val viewFactory: ViewFactory<FragmentKeyT> = TestViewFactory(),
) : FeatureFactory<Unit, FragmentKeyT> {
    override fun initialize(dependencies: Unit, key: FragmentKeyT): Feature {
        return Feature(
            state = Observable.empty(),
            viewFactory = viewFactory,
        )
    }
}
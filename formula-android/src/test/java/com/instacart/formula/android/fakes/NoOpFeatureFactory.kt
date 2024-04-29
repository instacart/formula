package com.instacart.formula.android.fakes

import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.FragmentKey
import io.reactivex.rxjava3.core.Observable

class NoOpFeatureFactory<FragmentKeyT : FragmentKey> : FeatureFactory<Unit, FragmentKeyT> {

    override fun initialize(dependencies: Unit, key: FragmentKeyT): Feature {
        return Feature(
            state = Observable.empty(),
            viewFactory = NoOpViewFactory()
        )
    }
}
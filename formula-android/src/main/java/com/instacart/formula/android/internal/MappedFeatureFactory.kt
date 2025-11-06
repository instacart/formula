package com.instacart.formula.android.internal

import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.RouteKey

@PublishedApi
internal class MappedFeatureFactory<Component, Dependencies, Key : RouteKey>(
    private val delegate: FeatureFactory<Dependencies, Key>,
    private val toDependencies: (Component) -> Dependencies,
) : FeatureFactory<Component, Key>() {
    override fun Params.initialize(): Feature {
        return delegate.initialize(
            dependencies = toDependencies(dependencies),
            routeId = routeId,
        )
    }
}
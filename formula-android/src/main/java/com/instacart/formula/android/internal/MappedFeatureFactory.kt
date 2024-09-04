package com.instacart.formula.android.internal

import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.FragmentKey

@PublishedApi
internal class MappedFeatureFactory<Component, Dependencies, Key : FragmentKey>(
    private val delegate: FeatureFactory<Dependencies, Key>,
    private val toDependencies: (Component) -> Dependencies,
) : FeatureFactory<Component, Key> {
    override fun initialize(dependencies: Component, key: Key): Feature {
        return delegate.initialize(
            dependencies = toDependencies(dependencies),
            key = key,
        )
    }
}
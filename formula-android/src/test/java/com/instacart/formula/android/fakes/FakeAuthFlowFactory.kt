package com.instacart.formula.android.fakes

import com.instacart.formula.android.Flow
import com.instacart.formula.android.FlowFactory
import com.instacart.formula.android.DisposableScope
import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.FragmentKey
import io.reactivex.rxjava3.core.Observable

class FakeAuthFlowFactory : FlowFactory<FakeComponent, FakeAuthFlowFactory.Component> {
    class Component(
        val onInitialized: (Component, FragmentKey) -> Unit
    )

    override fun createComponent(dependencies: FakeComponent): DisposableScope<Component> {
        return dependencies.createAuthFlowComponent()
    }

    override fun createFlow(): Flow<Component> {
        return Flow.build {
            bind(TestFeatureFactory<TestLoginFragmentKey>())
            bind(TestFeatureFactory<TestSignUpFragmentKey>())
        }
    }

    class TestFeatureFactory<FragmentKeyT : FragmentKey> : FeatureFactory<Component, FragmentKeyT> {
        override fun initialize(dependencies: Component, key: FragmentKeyT): Feature {
            dependencies.onInitialized(dependencies, key)
            return Feature(
                state = Observable.empty(),
                viewFactory = NoOpViewFactory()
            )
        }
    }
}
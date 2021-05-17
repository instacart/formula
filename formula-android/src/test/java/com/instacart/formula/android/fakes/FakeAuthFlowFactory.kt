package com.instacart.formula.android.fakes

import com.instacart.formula.android.Flow
import com.instacart.formula.android.FlowFactory
import com.instacart.formula.android.FragmentContract
import com.instacart.formula.android.DisposableScope
import io.reactivex.rxjava3.core.Observable

class FakeAuthFlowFactory : FlowFactory<FakeComponent, FakeAuthFlowFactory.Component> {
    class Component(
        val onInitialized: (Component, FragmentContract<*>) -> Unit
    )

    override fun createComponent(dependencies: FakeComponent): DisposableScope<Component> {
        return dependencies.createAuthFlowComponent()
    }

    override fun createFlow(): Flow<Component> {
        return Flow.build {
            bind { component, key: TestLoginFragmentContract ->
                component.onInitialized(component, key)
                Observable.empty<String>()
            }

            bind { component, key: TestSignUpFragmentContract ->
                component.onInitialized(component, key)
                Observable.empty<String>()
            }
        }
    }
}
package com.instacart.formula.integration

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentEnvironment
import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.integration.test.TestAccountFragmentContract
import com.instacart.formula.integration.test.TestLoginFragmentContract
import com.instacart.formula.integration.test.TestSignUpFragmentContract
import io.reactivex.Observable
import org.junit.Test

class FragmentFlowStoreTest {
    class AppComponent {
        val initialized = mutableListOf<Pair<AuthFlowDeclaration.Component, FragmentContract<*>>>()

        fun createAuthFlowComponent(): DisposableScope<AuthFlowDeclaration.Component> {
            val component = AuthFlowDeclaration.Component(onInitialized = { component, key ->
                initialized.add(component to key)
            })
            return DisposableScope(component, {
                initialized.clear()
            })
        }
    }

    class AuthFlowDeclaration : FlowDeclaration<AuthFlowDeclaration.Component>() {
        class Component(
            val onInitialized: (Component, FragmentContract<*>) -> Unit
        )

        override fun createFlow(): Flow<Component> {
            return build {
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

    class AuthFlowIntegration : FlowIntegration<AppComponent, AuthFlowDeclaration.Component>() {
        override val flowDeclaration = AuthFlowDeclaration()

        override fun createComponent(parentComponent: AppComponent): DisposableScope<AuthFlowDeclaration.Component> {
            return parentComponent.createAuthFlowComponent()
        }
    }

    @Test fun `component is shared between flow integrations`() {
        val appComponent = AppComponent()
        val store = createStore(appComponent)
        store
            .state(FragmentEnvironment())
            .test()
            .apply {
                store.onLifecycleEffect(LifecycleEvent.Added(TestLoginFragmentContract()))
                store.onLifecycleEffect(LifecycleEvent.Added(TestSignUpFragmentContract()))
            }

        val components = appComponent.initialized.map { it.first }
        assertThat(components).hasSize(2)
        assertThat(components[0]).isEqualTo(components[1])
    }

    @Test fun `component is disposed once flow exits`() {
        val appComponent = AppComponent()
        val store = createStore(appComponent)
        store
            .state(FragmentEnvironment())
            .test()
            .apply {
                store.onLifecycleEffect(LifecycleEvent.Added(TestLoginFragmentContract()))
                store.onLifecycleEffect(LifecycleEvent.Added(TestSignUpFragmentContract()))
            }
            .apply {
                assertThat(appComponent.initialized).hasSize(2)
            }
            .apply {
                store.onLifecycleEffect(LifecycleEvent.Removed(TestSignUpFragmentContract()))
                store.onLifecycleEffect(LifecycleEvent.Removed(TestLoginFragmentContract()))
            }
            .apply {
                assertThat(appComponent.initialized).hasSize(0)
            }
    }

    @Test fun `component is alive if we enter another integration`() {
        val appComponent = AppComponent()
        val store = createStore(appComponent)
        store
            .state(FragmentEnvironment())
            .test()
            .apply {
                store.onLifecycleEffect(LifecycleEvent.Added(TestLoginFragmentContract()))
                store.onLifecycleEffect(LifecycleEvent.Added(TestSignUpFragmentContract()))
                store.onLifecycleEffect(LifecycleEvent.Added(TestAccountFragmentContract()))
            }
            .apply {
                assertThat(appComponent.initialized).hasSize(2)
            }
    }

    fun createStore(component: AppComponent): FragmentFlowStore {
        return FragmentFlowStore.init(component) {
            bind(AuthFlowIntegration())
        }
    }
}

package com.instacart.formula.integration

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentEnvironment
import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.fragment.FragmentLifecycleEvent
import com.instacart.formula.integration.test.TestAccountFragmentContract
import com.instacart.formula.integration.test.TestLoginFragmentContract
import com.instacart.formula.integration.test.TestSignUpFragmentContract
import io.reactivex.rxjava3.core.Observable
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
                store.onLifecycleEffect(FragmentLifecycleEvent.Added(TestLoginFragmentContract()))
                store.onLifecycleEffect(FragmentLifecycleEvent.Added(TestSignUpFragmentContract()))
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
                store.onLifecycleEffect(FragmentLifecycleEvent.Added(TestLoginFragmentContract()))
                store.onLifecycleEffect(FragmentLifecycleEvent.Added(TestSignUpFragmentContract()))
            }
            .apply {
                assertThat(appComponent.initialized).hasSize(2)
            }
            .apply {
                store.onLifecycleEffect(FragmentLifecycleEvent.Removed(TestSignUpFragmentContract()))
                store.onLifecycleEffect(FragmentLifecycleEvent.Removed(TestLoginFragmentContract()))
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
                store.onLifecycleEffect(FragmentLifecycleEvent.Added(TestLoginFragmentContract()))
                store.onLifecycleEffect(FragmentLifecycleEvent.Added(TestSignUpFragmentContract()))
                store.onLifecycleEffect(FragmentLifecycleEvent.Added(TestAccountFragmentContract()))
            }
            .apply {
                assertThat(appComponent.initialized).hasSize(2)
            }
    }

    @Test fun `duplicate contract registration throws an exception`() {
        var exception: Throwable? = null
        try {
            FragmentFlowStore.init(AppComponent()) {
                bind(AuthFlowIntegration())
                bind(AuthFlowIntegration())
            }
        } catch (t: Throwable) {
            exception = t
        }
        assertThat(exception?.message).isEqualTo(
            "Binding for class com.instacart.formula.integration.test.TestLoginFragmentContract already exists"
        )
    }

    @Test fun `unsubscribe disposes of component`() {
        val appComponent = AppComponent()
        val store = createStore(appComponent)
        store
            .state(FragmentEnvironment())
            .test()
            .apply {
                store.onLifecycleEffect(FragmentLifecycleEvent.Added(TestLoginFragmentContract()))
            }
            .apply {
                assertThat(appComponent.initialized).hasSize(1)
            }
            .dispose()
            .apply {
                assertThat(appComponent.initialized).hasSize(0)
            }
    }

    fun createStore(component: AppComponent): FragmentFlowStore {
        return FragmentFlowStore.init(component) {
            bind(AuthFlowIntegration())
        }
    }
}

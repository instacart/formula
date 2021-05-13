package com.instacart.formula.android

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.fakes.DetailKey
import com.instacart.formula.android.fakes.FakeAuthFlowFactory
import com.instacart.formula.android.fakes.FakeComponent
import com.instacart.formula.android.fakes.MainKey
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentEnvironment
import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.fragment.FragmentLifecycleEvent
import com.instacart.formula.android.fakes.TestAccountFragmentContract
import com.instacart.formula.android.fakes.TestLoginFragmentContract
import com.instacart.formula.android.fakes.TestSignUpFragmentContract
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Test

class FragmentFlowStoreTest {

    @Test fun `duplicate contract registration throws an exception`() {
        var exception: Throwable? = null
        try {
            FragmentFlowStore.init(FakeComponent()) {
                bind(FakeAuthFlowFactory())
                bind(FakeAuthFlowFactory())
            }
        } catch (t: Throwable) {
            exception = t
        }
        assertThat(exception?.message).isEqualTo(
            "Binding for class com.instacart.formula.android.fakes.TestLoginFragmentContract already exists"
        )
    }

    @Test fun `component is shared between flow features`() {
        val appComponent = FakeComponent()
        val store = createStore(appComponent)
        store
            .state(FragmentEnvironment())
            .test()
            .apply {
                store.onLifecycleEffect(TestLoginFragmentContract().asAddedEvent())
                store.onLifecycleEffect(TestSignUpFragmentContract().asAddedEvent())
            }

        val components = appComponent.initialized.map { it.first }
        assertThat(components).hasSize(2)
        assertThat(components[0]).isEqualTo(components[1])
    }

    @Test fun `component is disposed once flow exits`() {
        val appComponent = FakeComponent()
        val store = createStore(appComponent)
        store
            .state(FragmentEnvironment())
            .test()
            .apply {
                store.onLifecycleEffect(TestLoginFragmentContract().asAddedEvent())
                store.onLifecycleEffect(TestSignUpFragmentContract().asAddedEvent())
            }
            .apply {
                assertThat(appComponent.initialized).hasSize(2)
            }
            .apply {
                store.onLifecycleEffect(TestSignUpFragmentContract().asRemovedEvent())
                store.onLifecycleEffect(TestLoginFragmentContract().asRemovedEvent())
            }
            .apply {
                assertThat(appComponent.initialized).hasSize(0)
            }
    }

    @Test fun `component is alive if we enter another feature`() {
        val appComponent = FakeComponent()
        val store = createStore(appComponent)
        store
            .state(FragmentEnvironment())
            .test()
            .apply {
                store.onLifecycleEffect(TestLoginFragmentContract().asAddedEvent())
                store.onLifecycleEffect(TestSignUpFragmentContract().asAddedEvent())
                store.onLifecycleEffect(TestAccountFragmentContract().asAddedEvent())
            }
            .apply {
                assertThat(appComponent.initialized).hasSize(2)
            }
    }

    @Test fun `unsubscribe disposes of component`() {
        val appComponent = FakeComponent()
        val store = createStore(appComponent)
        store
            .state(FragmentEnvironment())
            .test()
            .apply {
                store.onLifecycleEffect(TestLoginFragmentContract().asAddedEvent())
            }
            .apply {
                assertThat(appComponent.initialized).hasSize(1)
            }
            .dispose()
            .apply {
                assertThat(appComponent.initialized).hasSize(0)
            }
    }

    @Test fun `subscribed to state until removed from backstack`() {
        val master = MainKey(1)
        val detail = DetailKey(1)

        val component = FakeComponent()
        val store = createStore(component)
        store
            .toStates()
            .apply {
                store.onLifecycleEffect(master.asAddedEvent())
                store.onLifecycleEffect(detail.asAddedEvent())

                component.updateRelay.accept(master to "main-update")
                store.onLifecycleEffect(detail.asRemovedEvent())
                store.onLifecycleEffect(master.asRemovedEvent())

                component.updateRelay.accept(master to "main-update-2")
            }
            .assertValues(
                expectedState(),
                expectedState(master to "main-1-state"),
                expectedState(master to "main-1-state", detail to "detail-1-state"),
                expectedState(master to "main-update", detail to "detail-1-state"),
                expectedState(master to "main-update"),
                expectedState()
            )
    }

    @Test fun `various fragments added`() {

        val component = FakeComponent()
        val store = createStore(component)
        store.toStates()
            .apply {
                store.onLifecycleEffect(MainKey(1).asAddedEvent())
                store.onLifecycleEffect(DetailKey(1).asAddedEvent())
                store.onLifecycleEffect(DetailKey(2).asAddedEvent())
            }
            .assertValues(
                expectedState(),
                expectedState(MainKey(1) to "main-1-state"),
                expectedState(MainKey(1) to "main-1-state", DetailKey(1) to "detail-1-state"),
                expectedState(
                    MainKey(1) to "main-1-state",
                    DetailKey(1) to "detail-1-state",
                    DetailKey(2) to "detail-2-state"
                )
            )
    }

    @Test fun `bind feature factory with to dependencies defined`() {
        val myFeatureFactory = object : FeatureFactory<String, MainKey> {
            override fun initialize(dependencies: String, key: MainKey): Feature<*> {
                return TestUtils.feature(
                    stateValue = dependencies
                )
            }
        }

        val store = FragmentFlowStore.init(100) {
            bind(myFeatureFactory) {
                "Dependency: $it"
            }
        }

        store.toStates()
            .apply { store.onLifecycleEffect(MainKey(1).asAddedEvent()) }
            .assertValues(
                expectedState(),
                expectedState(MainKey(1) to "Dependency: 100")
            )
    }

    private fun FragmentFlowStore.toStates(): TestObserver<Map<FragmentKey, FragmentState>> {
        return state(FragmentEnvironment())
            .map { it.states.mapKeys { entry -> entry.key.key } }
            .test()
    }

    private fun expectedState(vararg states: Pair<FragmentContract<*>, *>): Map<FragmentKey, FragmentState> {
        return expectedState(states.asList())
    }

    private fun expectedState(states: List<Pair<FragmentContract<*>, *>>): Map<FragmentKey, FragmentState> {
        val initial = mutableMapOf<FragmentKey, FragmentState>()
        return states.foldRight(initial) { value, acc ->
            if (value.second != null) {
                acc.put(value.first, FragmentState(value.first, value.second!!))
            }

            acc
        }
    }

    fun createStore(component: FakeComponent): FragmentFlowStore {
        return FragmentFlowStore.init(component) {
            bind(FakeAuthFlowFactory())

            bind(MainKey::class) { component, key ->
                component.state(key)
            }

            bind(DetailKey::class) { component, key ->
                component.state(key)
            }
        }
    }

    private fun FragmentKey.asAddedEvent() = FragmentLifecycleEvent.Added(FragmentId("", this))
    private fun FragmentKey.asRemovedEvent() = FragmentLifecycleEvent.Removed(FragmentId("", this))
}

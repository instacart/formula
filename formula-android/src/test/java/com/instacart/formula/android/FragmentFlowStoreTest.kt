package com.instacart.formula.android

import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.fakes.DetailKey
import com.instacart.formula.android.fakes.FakeAuthFlowFactory
import com.instacart.formula.android.fakes.FakeComponent
import com.instacart.formula.android.fakes.MainKey
import com.instacart.formula.android.events.FragmentLifecycleEvent
import com.instacart.formula.android.fakes.NoOpViewFactory
import com.instacart.formula.android.fakes.TestAccountFragmentKey
import com.instacart.formula.android.fakes.TestLoginFragmentKey
import com.instacart.formula.android.fakes.TestSignUpFragmentKey
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
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
            "Binding for class com.instacart.formula.android.fakes.TestLoginFragmentKey already exists"
        )
    }

    @Test fun `component is shared between flow features`() {
        val appComponent = FakeComponent()
        val store = createStore(appComponent)
        store
            .state(FragmentEnvironment())
            .test()
            .apply {
                store.onLifecycleEffect(TestLoginFragmentKey().asAddedEvent())
                store.onLifecycleEffect(TestSignUpFragmentKey().asAddedEvent())
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
                store.onLifecycleEffect(TestLoginFragmentKey().asAddedEvent())
                store.onLifecycleEffect(TestSignUpFragmentKey().asAddedEvent())
            }
            .apply {
                assertThat(appComponent.initialized).hasSize(2)
            }
            .apply {
                store.onLifecycleEffect(TestSignUpFragmentKey().asRemovedEvent())
                store.onLifecycleEffect(TestLoginFragmentKey().asRemovedEvent())
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
                store.onLifecycleEffect(TestLoginFragmentKey().asAddedEvent())
                store.onLifecycleEffect(TestSignUpFragmentKey().asAddedEvent())
                store.onLifecycleEffect(TestAccountFragmentKey().asAddedEvent())
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
                store.onLifecycleEffect(TestLoginFragmentKey().asAddedEvent())
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

    @Test fun `background feature events are moved to the main thread`() {
        val executor = Executors.newSingleThreadExecutor()
        val component = FakeComponent()
        val store = createStore(component)

        val latch = CountDownLatch(1)

        val updates = mutableListOf<Map<FragmentKey, Any>>()
        val updateThreads = linkedSetOf<Thread>()
        val disposable = store.state(FragmentEnvironment()).subscribe {
            val states = it.states.mapKeys { it.key.key }.mapValues { it.value.renderModel }
            updates.add(states)

            updateThreads.add(Thread.currentThread())
        }

        // Add couple of features
        store.onLifecycleEffect(MainKey(1).asAddedEvent())
        store.onLifecycleEffect(DetailKey(2).asAddedEvent())

        // Pass feature updates on a background thread
        executor.execute {
            component.updateRelay.accept(MainKey(1) to "main-state-1")
            component.updateRelay.accept(MainKey(1) to "main-state-2")
            component.updateRelay.accept(MainKey(1) to "main-state-3")

            component.updateRelay.accept(DetailKey(2) to "detail-state-1")
            component.updateRelay.accept(DetailKey(2) to "detail-state-2")
            component.updateRelay.accept(DetailKey(2) to "detail-state-3")
            latch.countDown()
        }

        // Wait for background execution to finish
        if(!latch.await(100, TimeUnit.MILLISECONDS)) {
            throw IllegalStateException("timeout")
        }

        Shadows.shadowOf(Looper.getMainLooper()).idle()

        val expected = mapOf(
            MainKey(1) to "main-state-3",
            DetailKey(2) to "detail-state-3"
        )

        val last = updates.last()
        assertThat(last).isEqualTo(expected)

        assertThat(updateThreads).hasSize(1)
        assertThat(updateThreads).containsExactly(Thread.currentThread())
    }

    private fun FragmentFlowStore.toStates(): TestObserver<Map<FragmentKey, FragmentState>> {
        return state(FragmentEnvironment())
            .map { it.states.mapKeys { entry -> entry.key.key } }
            .test()
    }

    private fun expectedState(vararg states: Pair<FragmentKey, *>): Map<FragmentKey, FragmentState> {
        return expectedState(states.asList())
    }

    private fun expectedState(states: List<Pair<FragmentKey, *>>): Map<FragmentKey, FragmentState> {
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

            bind(TestFeatureFactory<MainKey>())
            bind(TestFeatureFactory<DetailKey>())
        }
    }

    private fun FragmentKey.asAddedEvent() = FragmentLifecycleEvent.Added(FragmentId("", this))
    private fun FragmentKey.asRemovedEvent() = FragmentLifecycleEvent.Removed(FragmentId("", this))

    class TestFeatureFactory<FragmentKeyT : FragmentKey>: FeatureFactory<FakeComponent, FragmentKeyT> {
        override fun initialize(dependencies: FakeComponent, key: FragmentKeyT): Feature<*> {
            return Feature(
                state = dependencies.state(key),
                viewFactory = NoOpViewFactory()
            )
        }
    }
}

package com.instacart.formula.android

import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.fakes.DetailKey
import com.instacart.formula.android.fakes.FakeComponent
import com.instacart.formula.android.fakes.MainKey
import com.instacart.formula.android.events.RouteLifecycleEvent
import com.instacart.testutils.android.TestViewFactory
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class NavigationStoreTest {

    private var store: NavigationStore? = null

    @After fun disposeStore() {
        store?.dispose()
    }

    @Test fun `duplicate contract registration throws an exception`() {
        var exception: Throwable? = null
        try {
            NavigationStore.Builder().build(FakeComponent()) {
                bind(TestFeatureFactory<MainKey>())
                bind(TestFeatureFactory<MainKey>())
            }
        } catch (t: Throwable) {
            exception = t
        }
        assertThat(exception?.message).isEqualTo(
            "Binding for class com.instacart.formula.android.fakes.MainKey already exists"
        )
    }

    @Test fun `subscribed to state until removed from backstack`() {
        val master = MainKey(1)
        val detail = DetailKey(1)

        val component = FakeComponent()
        val store = createStore(component)
        assertState(store)
        store.onLifecycleEvent(master.asAddedEvent())
        assertState(store, master to "main-1-state")
        store.onLifecycleEvent(detail.asAddedEvent())
        assertState(store, master to "main-1-state", detail to "detail-1-state")

        component.updateState(master, "main-update")
        assertState(store, master to "main-update", detail to "detail-1-state")
        store.onLifecycleEvent(detail.asRemovedEvent())
        assertState(store, master to "main-update")
        store.onLifecycleEvent(master.asRemovedEvent())
        assertState(store)
    }

    @Test fun `various fragments added`() {

        val component = FakeComponent()
        val store = createStore(component)
        assertState(store)
        store.onLifecycleEvent(MainKey(1).asAddedEvent())
        assertState(store, MainKey(1) to "main-1-state")
        store.onLifecycleEvent(DetailKey(1).asAddedEvent())
        assertState(store, MainKey(1) to "main-1-state", DetailKey(1) to "detail-1-state")
        store.onLifecycleEvent(DetailKey(2).asAddedEvent())
        assertState(
            store,
            MainKey(1) to "main-1-state",
            DetailKey(1) to "detail-1-state",
            DetailKey(2) to "detail-2-state"
        )
    }

    @Test fun `bind feature factory with to dependencies defined`() {
        val myFeatureFactory = object : FeatureFactory<String, MainKey>() {
            override fun Params.initialize(): Feature {
                return TestUtils.feature(
                    stateValue = dependencies
                )
            }
        }

        val store = track(NavigationStore.Builder().build(100) {
            bind(myFeatureFactory) {
                "Dependency: $it"
            }
        })

        store.onLifecycleEvent(MainKey(1).asAddedEvent())
        assertState(store, MainKey(1) to "Dependency: 100")
    }

    @Test fun `background feature events are moved to the main thread`() {
        val executor = Executors.newSingleThreadExecutor()
        val component = FakeComponent()
        val latch = CountDownLatch(1)
        val updateThreads = linkedSetOf<Thread>()
        val store = createStore(component) {
            updateThreads.add(Thread.currentThread())
        }

        // Add couple of features
        store.onLifecycleEvent(MainKey(1).asAddedEvent())
        store.onLifecycleEvent(DetailKey(2).asAddedEvent())

        // Pass feature updates on a background thread
        executor.execute {
            component.updateState(MainKey(1), "main-state-1")
            component.updateState(MainKey(1), "main-state-2")
            component.updateState(MainKey(1), "main-state-3")

            component.updateState(DetailKey(2), "detail-state-1")
            component.updateState(DetailKey(2), "detail-state-2")
            component.updateState(DetailKey(2), "detail-state-3")
            latch.countDown()
        }

        // Wait for background execution to finish
        if(!latch.await(100, TimeUnit.MILLISECONDS)) {
            throw IllegalStateException("timeout")
        }

        Shadows.shadowOf(Looper.getMainLooper()).idle()

        assertState(
            store,
            MainKey(1) to "main-state-3",
            DetailKey(2) to "detail-state-3",
        )

        assertThat(updateThreads).containsExactly(Thread.currentThread())
        executor.shutdownNow()
    }

    @Test fun `store returns missing binding event when no feature factory is present`() {
        val store = track(NavigationStore.Builder().build(FakeComponent()) {
            bind(TestFeatureFactory<MainKey>())
        })
        val routeId = RouteId(
            instanceId = "random",
            key = DetailKey(id = 100)
        )
        store.onLifecycleEvent(
            RouteLifecycleEvent.Added(routeId = routeId)
        )

        assertThat(store.stateFlow.value.features[routeId]).isEqualTo(
            FeatureEvent.MissingBinding(routeId)
        )
    }

    @Test fun `store returns failure event when feature factory initialization throws an error`() {
        val expectedError = RuntimeException("something happened")
        val store = track(NavigationStore.Builder().build(FakeComponent()) {
            val featureFactory = object : FeatureFactory<FakeComponent, MainKey>() {
                override fun Params.initialize(): Feature {
                    throw expectedError
                }
            }
            bind(featureFactory)
        })

        val routeId = RouteId(
            instanceId = "random",
            key = MainKey(id = 100)
        )
        store.onLifecycleEvent(
            RouteLifecycleEvent.Added(routeId = routeId)
        )

        assertThat(store.stateFlow.value.features[routeId]).isEqualTo(
            FeatureEvent.Failure(routeId, expectedError)
        )
    }

    @Test fun `route ignores events after removal`() {
        val stateFlow = MutableStateFlow<Any>("value")

        val store = track(NavigationStore.Builder().build {
            val featureFactory = object : FeatureFactory<Any, MainKey>() {
                override fun Params.initialize(): Feature {
                    return Feature(
                        viewFactory = TestViewFactory(),
                    ) { stateFlow }
                }
            }
            bind(featureFactory)
        })

        val routeId = RouteId("", MainKey(1))
        store.onLifecycleEvent(
            RouteLifecycleEvent.Added(routeId = routeId)
        )

        // Check that initial value is present
        val firstModel = store.stateFlow.value.outputs[routeId]?.renderModel
        assertThat(firstModel).isEqualTo("value")

        // Remove fragment
        store.onLifecycleEvent(
            RouteLifecycleEvent.Removed(routeId = routeId)
        )

        // Check that new events are ignored
        stateFlow.value = "new-value"

        // Output should not exist
        val secondModel = store.stateFlow.value.outputs[routeId]
        assertThat(secondModel).isNull()
    }

    @Test fun `feature factory crash emits on screen error`() {
        val error = RuntimeException("factory crashed")

        val screenErrors = mutableListOf<Pair<RouteKey, Throwable>>()
        val environment = RouteEnvironment(
            onScreenError = { key, err ->
                screenErrors.add(key to err)
            }
        )

        val store = track(NavigationStore.Builder()
            .setRouteEnvironment(environment)
            .build {
                val featureFactory = object : FeatureFactory<Any, MainKey>() {
                    override fun Params.initialize(): Feature {
                        return Feature(
                            viewFactory = TestViewFactory(),
                        ) {
                            throw error
                        }
                    }
                }
                bind(featureFactory)
            })

        val routeId = RouteId("", MainKey(1))
        store.onLifecycleEvent(
            RouteLifecycleEvent.Added(routeId = routeId)
        )

        // No output should be emitted for the crashed feature
        val model = store.stateFlow.value.outputs[routeId]?.renderModel
        assertThat(model).isNull()

        // Error was reported via onScreenError
        assertThat(screenErrors).hasSize(1)
        assertThat(screenErrors.first().first).isEqualTo(routeId.key)
        assertThat(screenErrors.first().second).isEqualTo(error)
    }

    @Test fun `state flow feature`() {
        val stateFlow = MutableStateFlow(0)
        val store = track(NavigationStore.Builder().build {
            bind(object : FeatureFactory<Any, MainKey>() {
                override fun Params.initialize(): Feature {
                    return Feature(TestViewFactory()) {
                        stateFlow
                    }
                }
            })
        })

        val key = MainKey(1)
        val routeId = RouteId("", key)
        store.onLifecycleEvent(RouteLifecycleEvent.Added(routeId = routeId))
        assertState(store, key to 0)

        stateFlow.value = 1
        assertState(store, key to 1)
        stateFlow.value = 2
        assertState(store, key to 2)
    }

    @Test fun `visible output follows route visibility`() {
        val stateFlow = MutableStateFlow<Any>("value")
        val store = track(NavigationStore.Builder().build {
            val featureFactory = object : FeatureFactory<Any, MainKey>() {
                override fun Params.initialize(): Feature {
                    return Feature(
                        viewFactory = TestViewFactory(),
                    ) { stateFlow }
                }
            }
            bind(featureFactory)
        })

        val routeId = RouteId("", MainKey(1))
        store.onLifecycleEvent(
            RouteLifecycleEvent.Added(routeId = routeId)
        )

        // No visible output yet
        val firstModel = store.stateFlow.value.visibleOutput()
        assertThat(firstModel).isNull()

        // Toggle visibility
        store.onVisibilityChanged(routeId, true)

        // Check that visible output is now present
        val secondModel = store.stateFlow.value.visibleOutput()
        assertThat(secondModel).isNotNull()

        // Toggle visibility again
        store.onVisibilityChanged(routeId, false)

        // Check that visible output is null again
        val third = store.stateFlow.value.visibleOutput()
        assertThat(third).isNull()
    }

    private fun assertState(store: NavigationStore, vararg states: Pair<RouteKey, *>) {
        val outputs = store.stateFlow.value.outputs.mapKeys { it.key.key }
        assertThat(outputs).isEqualTo(expectedState(*states))
    }

    private fun expectedState(vararg states: Pair<RouteKey, *>): Map<RouteKey, RouteOutput> {
        return expectedState(states.asList())
    }

    private fun expectedState(states: List<Pair<RouteKey, *>>): Map<RouteKey, RouteOutput> {
        val initial = mutableMapOf<RouteKey, RouteOutput>()
        return states.foldRight(initial) { value, acc ->
            if (value.second != null) {
                acc.put(value.first, RouteOutput(value.first, value.second!!))
            }

            acc
        }
    }

    private fun createStore(
        component: FakeComponent,
        onPreRenderNavigationState: ((NavigationState) -> Unit)? = null,
    ): NavigationStore {
        return track(
            NavigationStore.Builder()
                .setOnPreRenderNavigationState(onPreRenderNavigationState)
                .build(component) {
                    bind(TestFeatureFactory<MainKey>())
                    bind(TestFeatureFactory<DetailKey>())
                }
        )
    }

    private fun track(store: NavigationStore): NavigationStore {
        this.store = store
        return store
    }

    private fun RouteKey.asAddedEvent() = RouteLifecycleEvent.Added(RouteId("", this))
    private fun RouteKey.asRemovedEvent() = RouteLifecycleEvent.Removed(RouteId("", this))

    class TestFeatureFactory<FragmentKeyT : RouteKey>: FeatureFactory<FakeComponent, FragmentKeyT>() {
        override fun Params.initialize(): Feature {
            return Feature(
                viewFactory = TestViewFactory(),
            ) {
                dependencies.getOrCreateState(key)
            }
        }
    }
}

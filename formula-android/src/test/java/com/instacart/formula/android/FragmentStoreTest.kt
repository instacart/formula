package com.instacart.formula.android

import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.fakes.DetailKey
import com.instacart.formula.android.fakes.FakeComponent
import com.instacart.formula.android.fakes.MainKey
import com.instacart.formula.android.events.FragmentLifecycleEvent
import com.instacart.testutils.android.TestViewFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.TestObserver
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FragmentStoreTest {

    @Test fun `duplicate contract registration throws an exception`() {
        var exception: Throwable? = null
        try {
            FragmentStore.Builder().build(FakeComponent()) {
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
        store
            .toStates()
            .apply {
                store.onLifecycleEvent(master.asAddedEvent())
                store.onLifecycleEvent(detail.asAddedEvent())

                component.updateRelay.accept(master to "main-update")
                store.onLifecycleEvent(detail.asRemovedEvent())
                store.onLifecycleEvent(master.asRemovedEvent())

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
                store.onLifecycleEvent(MainKey(1).asAddedEvent())
                store.onLifecycleEvent(DetailKey(1).asAddedEvent())
                store.onLifecycleEvent(DetailKey(2).asAddedEvent())
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
        val myFeatureFactory = object : FeatureFactory<String, MainKey>() {
            override fun Params.initialize(): Feature {
                return TestUtils.feature(
                    stateValue = dependencies
                )
            }
        }

        val store = FragmentStore.Builder().build(100) {
            bind(myFeatureFactory) {
                "Dependency: $it"
            }
        }

        store.toStates()
            .apply { store.onLifecycleEvent(MainKey(1).asAddedEvent()) }
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
        val disposable = store.state().subscribe {
            val states = it.outputs.mapKeys { it.key.key }.mapValues { it.value.renderModel }
            updates.add(states)

            updateThreads.add(Thread.currentThread())
        }

        // Add couple of features
        store.onLifecycleEvent(MainKey(1).asAddedEvent())
        store.onLifecycleEvent(DetailKey(2).asAddedEvent())

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

    @Test fun `store returns missing binding event when no feature factory is present`() {
        val store = FragmentStore.Builder().build(FakeComponent()) {
            bind(TestFeatureFactory<MainKey>())
        }
        val observer = store.state().test()
        val fragmentId = FragmentId(
            instanceId = "random",
            key = DetailKey(id = 100)
        )
        store.onLifecycleEvent(
            FragmentLifecycleEvent.Added(fragmentId = fragmentId)
        )

        val lastState = observer.values().last()
        assertThat(lastState.features[fragmentId]).isEqualTo(
            FeatureEvent.MissingBinding(fragmentId)
        )
    }

    @Test fun `store returns failure event when feature factory initialization throws an error`() {
        val expectedError = RuntimeException("something happened")
        val store = FragmentStore.Builder().build(FakeComponent()) {
            val featureFactory = object : FeatureFactory<FakeComponent, MainKey>() {
                override fun Params.initialize(): Feature {
                    throw expectedError
                }
            }
            bind(featureFactory)
        }

        val observer = store.state().test()
        val fragmentId = FragmentId(
            instanceId = "random",
            key = MainKey(id = 100)
        )
        store.onLifecycleEvent(
            FragmentLifecycleEvent.Added(fragmentId = fragmentId)
        )

        val lastState = observer.values().last()
        assertThat(lastState.features[fragmentId]).isEqualTo(
            FeatureEvent.Failure(fragmentId, expectedError)
        )
    }

    @Test fun `fragment store ignores events after key is removed`() {
        val stateSubject = PublishSubject.create<Any>()

        val store = FragmentStore.Builder().build {
            val featureFactory = object : FeatureFactory<Any, MainKey>() {
                override fun Params.initialize(): Feature {
                    return Feature(
                        state = stateSubject,
                        viewFactory = TestViewFactory(),
                    )
                }
            }
            bind(featureFactory)
        }

        val observer = store.state().test()
        val fragmentId = FragmentId("", MainKey(1))
        store.onLifecycleEvent(
            FragmentLifecycleEvent.Added(fragmentId = fragmentId)
        )
        stateSubject.onNext("value")

        // Check that first event was shown
        val firstModel = observer.values().last().outputs[fragmentId]?.renderModel
        assertThat(firstModel).isEqualTo("value")

        // Remove fragment
        store.onLifecycleEvent(
            FragmentLifecycleEvent.Removed(fragmentId = fragmentId)
        )

        // Check that new events are ignored
        stateSubject.onNext("new-value")

        // Output should not exist
        val secondModel = observer.values().last().outputs[fragmentId]
        assertThat(secondModel).isNull()
    }

    @Test fun `feature observable error emits on screen error and finishes`() {
        val stateSubject = PublishSubject.create<Any>()

        val screenErrors = mutableListOf<Pair<FragmentKey, Throwable>>()
        val environment = FragmentEnvironment(
            onScreenError = { key, error ->
                screenErrors.add(key to error)
            }
        )

        val store = FragmentStore.Builder()
            .setFragmentEnvironment(environment)
            .build {
                val featureFactory = object : FeatureFactory<Any, MainKey>() {
                    override fun Params.initialize(): Feature {
                        return Feature(
                            state = stateSubject,
                            viewFactory = TestViewFactory(),
                        )
                    }
                }
                bind(featureFactory)
            }


        val observer = store.state().test()
        val fragmentId = FragmentId("", MainKey(1))
        store.onLifecycleEvent(
            FragmentLifecycleEvent.Added(fragmentId = fragmentId)
        )
        stateSubject.onNext("value")

        val firstModel = observer.values().last().outputs[fragmentId]?.renderModel
        assertThat(firstModel).isEqualTo("value")

        // Emit error
        val error = RuntimeException("error")
        stateSubject.onError(error)

        // Model didn't change
        val secondModel = observer.values().last().outputs[fragmentId]?.renderModel
        assertThat(secondModel).isEqualTo("value")

        // Store observable didn't crash
        observer.assertNoErrors()

        assertThat(screenErrors).containsExactly(
            fragmentId.key to error
        )
    }

    @Test fun `state flow feature`() {
        val stateFlow = MutableStateFlow(0)
        val store = FragmentStore.Builder().build {
            bind(object : FeatureFactory<Any, MainKey>() {
                override fun Params.initialize(): Feature {
                    return Feature(TestViewFactory()) {
                        stateFlow
                    }
                }
            })
        }

        val observer = store.toStates()
        
        // Add fragment key
        val key = MainKey(1)
        val fragmentId = FragmentId("", key)
        store.onLifecycleEvent(FragmentLifecycleEvent.Added(fragmentId = fragmentId))

        stateFlow.tryEmit(1)
        stateFlow.tryEmit(2)

        observer.assertValues(
            expectedState(),
            expectedState(key to 0),
            expectedState(key to 1),
            expectedState(key to 2),
        )
    }

    @Test fun `fragment store visible output`() {
        val store = FragmentStore.Builder().build {
            val featureFactory = object : FeatureFactory<Any, MainKey>() {
                override fun Params.initialize(): Feature {
                    return Feature(
                        state = Observable.just("value"),
                        viewFactory = TestViewFactory(),
                    )
                }
            }
            bind(featureFactory)
        }

        val observer = store.state().test()
        val fragmentId = FragmentId("", MainKey(1))
        store.onLifecycleEvent(
            FragmentLifecycleEvent.Added(fragmentId = fragmentId)
        )

        // No visible output yet
        val firstModel = observer.values().last().visibleOutput()
        assertThat(firstModel).isNull()

        // Toggle visibility
        store.onVisibilityChanged(fragmentId, true)

        // Check that visible output is now present
        val secondModel = observer.values().last().visibleOutput()
        assertThat(secondModel).isNotNull()

        // Toggle visibility again
        store.onVisibilityChanged(fragmentId, false)

        // Check that visible output is null again
        val third = observer.values().last().visibleOutput()
        assertThat(third).isNull()
    }
    
    private fun FragmentStore.toStates(): TestObserver<Map<FragmentKey, FragmentOutput>> {
        return state()
            .map { it.outputs.mapKeys { entry -> entry.key.key } }
            .test()
    }

    private fun expectedState(vararg states: Pair<FragmentKey, *>): Map<FragmentKey, FragmentOutput> {
        return expectedState(states.asList())
    }

    private fun expectedState(states: List<Pair<FragmentKey, *>>): Map<FragmentKey, FragmentOutput> {
        val initial = mutableMapOf<FragmentKey, FragmentOutput>()
        return states.foldRight(initial) { value, acc ->
            if (value.second != null) {
                acc.put(value.first, FragmentOutput(value.first, value.second!!))
            }

            acc
        }
    }

    fun createStore(component: FakeComponent): FragmentStore {
        return FragmentStore.Builder().build(component) {
            bind(TestFeatureFactory<MainKey>())
            bind(TestFeatureFactory<DetailKey>())
        }
    }

    private fun FragmentKey.asAddedEvent() = FragmentLifecycleEvent.Added(FragmentId("", this))
    private fun FragmentKey.asRemovedEvent() = FragmentLifecycleEvent.Removed(FragmentId("", this))

    class TestFeatureFactory<FragmentKeyT : FragmentKey>: FeatureFactory<FakeComponent, FragmentKeyT>() {
        override fun Params.initialize(): Feature {
            return Feature(
                state = dependencies.state(key),
                viewFactory = TestViewFactory()
            )
        }
    }
}

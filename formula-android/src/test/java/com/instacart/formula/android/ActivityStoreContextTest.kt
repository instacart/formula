package com.instacart.formula.android

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.internal.ActivityStoreContextImpl
import kotlinx.coroutines.test.runTest
import kotlinx.parcelize.Parcelize
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class ActivityStoreContextTest {
    class FakeActivity : FragmentActivity() {
        fun doSomething() {}
    }

    private val context: ActivityStoreContextImpl<FakeActivity> = ActivityStoreContextImpl()

    @Test fun `send drops events if activity is not started`() {
        val activity = createFakeActivity()
        context.attachActivity(activity)

        context.send {
            doSomething()
        }

        verifyNoInteractions(activity)
    }

    @Test fun `send event success`() {
        val activity = createFakeActivity()
        context.attachActivity(activity)
        context.onActivityStarted(activity)

        context.send {
            doSomething()
        }

        verify(activity).doSomething()
    }

    @Test fun `is fragment started`() = runTest {
        val contract = createContract()
        context.isRouteStarted(contract).test {
            // Initial emission reflects the current (not started) state.
            assertThat(awaitItem()).isFalse()

            val instance = RouteId("", contract)
            context.updateRouteLifecycleState(instance, Lifecycle.State.STARTED)
            assertThat(awaitItem()).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `isFragmentStarted emits latest value first`() = runTest {
        val contract = createContract()
        val fragment = RouteId("", contract)

        // 1st subscription
        context.isRouteStarted(contract).test {
            assertThat(awaitItem()).isFalse()
            context.updateRouteLifecycleState(fragment, Lifecycle.State.STARTED)
            assertThat(awaitItem()).isTrue()
            cancelAndIgnoreRemainingEvents()
        }

        // 2nd subscription - should receive latest value first
        context.isRouteStarted(contract).test {
            assertThat(awaitItem()).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `is fragment resumed`() = runTest {
        val contract = createContract()
        context.isRouteResumed(contract).test {
            // Initial emission reflects the current (not resumed) state.
            assertThat(awaitItem()).isFalse()

            val instance = RouteId("", contract)
            context.updateRouteLifecycleState(instance, Lifecycle.State.RESUMED)
            assertThat(awaitItem()).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `navigation store forwards route lifecycle state to context`() = runTest {
        // Mirror the ActivityManager wiring so a non-Fragment host (e.g. Compose Nav 3) can drive
        // route lifecycle state through the NavigationStore seam.
        val store = NavigationStore.EMPTY
        store.onRouteLifecycleState = context::updateRouteLifecycleState

        val contract = createContract()
        context.isRouteStarted(contract).test {
            assertThat(awaitItem()).isFalse()

            store.onRouteLifecycleStateChanged(RouteId("", contract), Lifecycle.State.STARTED)
            assertThat(awaitItem()).isTrue()

            store.onRouteLifecycleStateChanged(RouteId("", contract), Lifecycle.State.CREATED)
            assertThat(awaitItem()).isFalse()

            store.onRouteLifecycleStateChanged(RouteId("", contract), Lifecycle.State.STARTED)
            assertThat(awaitItem()).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `attaching a new activity instance resets stale route state for a new subscription`() = runTest {
        val contract = createContract()
        context.updateRouteLifecycleState(RouteId("", contract), Lifecycle.State.STARTED)

        // Simulates configuration change: the delegate is reused, but route lifecycle state from the
        // previous activity instance must not replay as `true` to the next host. Resetting on attach
        // (rather than on the old activity's destroy) is safe under overlapping activity lifecycles.
        context.attachActivity(createFakeActivity())

        context.isRouteStarted(contract).test {
            assertThat(awaitItem()).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `clearRouteLifecycleState flips active subscriptions back to false`() = runTest {
        val contract = createContract()
        context.isRouteStarted(contract).test {
            assertThat(awaitItem()).isFalse()

            context.updateRouteLifecycleState(RouteId("", contract), Lifecycle.State.STARTED)
            assertThat(awaitItem()).isTrue()

            context.clearRouteLifecycleState()
            assertThat(awaitItem()).isFalse()

            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createContract(): RouteKey {
        return TestRouteKey()
    }

    private fun createFakeActivity(): FakeActivity {
        return mock<FakeActivity>()
    }

    @Parcelize
    private data class TestRouteKey(
        override val tag: String = "fake tag",
    ) : RouteKey
}

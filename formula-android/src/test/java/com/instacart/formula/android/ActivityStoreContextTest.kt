package com.instacart.formula.android

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.instacart.formula.android.internal.ActivityStoreContextImpl
import kotlinx.coroutines.rx3.asObservable
import kotlinx.parcelize.Parcelize
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class ActivityStoreContextTest {
    class FakeActivity : FragmentActivity() {
        fun doSomething() {}
    }

    private lateinit var context: ActivityStoreContextImpl<FakeActivity>

    @Before fun setup() {
        context = ActivityStoreContextImpl()
    }

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

    @Test fun `is fragment started`() {
        val contract = createContract()
        context.isRouteStarted(contract)
            .asObservable()
            .test()
            .apply {
                val instance = RouteId("", contract)
                context.updateRouteLifecycleState(instance, Lifecycle.State.STARTED)
            }
            .assertValues(false, true)
    }

    @Test fun `isFragmentStarted emits latest value first`() {
        val contract = createContract()
        val fragment = RouteId("", contract)
        val observable = context.isRouteStarted(contract).asObservable()

        // 1st subscription
        observable
            .test()
            .apply {
                context.updateRouteLifecycleState(fragment, Lifecycle.State.STARTED)
            }
            .assertValues(false, true)

        // 2nd subscription - should receive latest emission first
        observable
            .test()
            .assertValues(true)
    }

    @Test fun `is fragment resumed`() {
        val contract = createContract()
        context.isRouteResumed(contract)
            .asObservable()
            .test()
            .apply {
                val instance = RouteId("", contract)
                context.updateRouteLifecycleState(instance, Lifecycle.State.RESUMED)
            }
            .assertValues(false, true)
    }

    @Test fun `navigation store forwards route lifecycle state to context`() {
        // Mirror the ActivityManager wiring so a non-Fragment host (e.g. Compose Nav 3) can drive
        // route lifecycle state through the NavigationStore seam.
        val store = NavigationStore.EMPTY
        store.onRouteLifecycleState = context::updateRouteLifecycleState

        val contract = createContract()
        context.isRouteStarted(contract)
            .asObservable()
            .test()
            .apply {
                store.onRouteLifecycleStateChanged(RouteId("", contract), Lifecycle.State.STARTED)
                store.onRouteLifecycleStateChanged(RouteId("", contract), Lifecycle.State.CREATED)
                store.onRouteLifecycleStateChanged(RouteId("", contract), Lifecycle.State.STARTED)
            }
            .assertValues(false, true, false, true)
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

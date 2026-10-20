package com.instacart.formula.android

import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.events.RouteLifecycleEvent
import com.instacart.formula.android.internal.ActivityStoreFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
class ActivityStoreFactoryTest {

    class FakeActivity : FragmentActivity()

    @Test fun `manager invokes onCleared to dispose activity resources`() {
        val navigationStore = FakeNavigationStore()
        val factory = ActivityStoreFactory(
            activities = {
                activity(FakeActivity::class) {
                    ActivityStore(
                        onCleared = navigationStore::dispose,
                    )
                }
            }
        )

        val manager = factory.init(mock<FakeActivity>())!!
        manager.dispose()

        assertThat(navigationStore.isDisposed).isTrue()
    }

    private class FakeNavigationStore : NavigationStore {
        override val environment = RouteEnvironment()
        override val stateFlow: StateFlow<NavigationState> = MutableStateFlow(NavigationState())
        var isDisposed = false
            private set

        override fun onLifecycleEvent(event: RouteLifecycleEvent) = Unit

        override fun onVisibilityChanged(routeId: RouteId<*>, visible: Boolean) = Unit

        override fun getViewFactory(routeId: RouteId<*>): ViewFactory<Any>? = null

        override fun dispose() {
            isDisposed = true
        }
    }

    @Test fun `returns null if no binding for activity is found`() {
        val factory = ActivityStoreFactory(
            activities = {}
        )

        val store = factory.init(mock<FakeActivity>())
        assertThat(store).isNull()
    }
}

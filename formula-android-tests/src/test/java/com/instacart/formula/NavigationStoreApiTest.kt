package com.instacart.formula

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.NavigationStore
import com.instacart.formula.android.RouteId
import com.instacart.formula.android.events.RouteLifecycleEvent
import com.instacart.testutils.android.TestKey
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationStoreApiTest {

    @Test fun `route lifecycle drives state flow output`() {
        val featureState = MutableStateFlow<Any>("initial")
        val store = NavigationStore.Builder().build {
            bind(
                TestFeatureFactory<TestKey>(
                    state = { { featureState } },
                )
            )
        }
        val routeId = RouteId("route-instance", TestKey())

        try {
            store.onLifecycleEvent(RouteLifecycleEvent.Added(routeId))
            assertThat(store.stateFlow.value.activeIds).contains(routeId)
            assertThat(store.stateFlow.value.outputs[routeId]?.renderModel).isEqualTo("initial")

            featureState.value = "updated"
            assertThat(store.stateFlow.value.outputs[routeId]?.renderModel).isEqualTo("updated")

            store.onLifecycleEvent(RouteLifecycleEvent.Removed(routeId))
            assertThat(store.stateFlow.value.activeIds).doesNotContain(routeId)
            assertThat(store.stateFlow.value.outputs).doesNotContainKey(routeId)
        } finally {
            store.dispose()
        }
    }
}

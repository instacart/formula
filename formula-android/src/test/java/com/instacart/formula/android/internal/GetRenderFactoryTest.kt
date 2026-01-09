package com.instacart.formula.android.internal

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureEvent
import com.instacart.formula.android.RouteEnvironment
import com.instacart.formula.android.RouteId
import com.instacart.formula.android.RouteKey
import com.instacart.formula.android.fakes.MainKey
import com.instacart.testutils.android.TestViewFactory
import io.reactivex.rxjava3.core.Observable
import org.junit.Test

class GetRenderFactoryTest {

    @Test fun `getRenderFactory with no feature`() {
        val screenErrors = mutableListOf<Pair<RouteKey, Throwable>>()
        val environment = RouteEnvironment(
            onScreenError = { key, error -> screenErrors.add(key to error) }
        )

        val routeId = RouteId("test", MainKey(1))
        val features = mapOf<RouteId<*>, FeatureEvent>()

        val viewFactory = features.getRenderFactory(environment, routeId)
        assertThat(viewFactory).isNull()

        assertThat(screenErrors).hasSize(1)
        assertThat(screenErrors.last().first).isEqualTo(MainKey(1))
        assertThat(screenErrors.last().second).hasMessageThat().contains("Could not find feature for ${routeId.key}")
    }

    @Test fun `getRenderFactory with missing binding`() {
        val screenErrors = mutableListOf<Pair<RouteKey, Throwable>>()
        val environment = RouteEnvironment(
            onScreenError = { key, error -> screenErrors.add(key to error) }
        )

        val routeId = RouteId("test", MainKey(1))
        val features: Map<RouteId<*>, FeatureEvent> = mapOf(
            routeId to FeatureEvent.MissingBinding(
                id = routeId,
            )
        )

        val viewFactory = features.getRenderFactory(environment, routeId)
        assertThat(viewFactory).isNull()

        assertThat(screenErrors).hasSize(1)
        assertThat(screenErrors.last().first).isEqualTo(MainKey(1))
        assertThat(screenErrors.last().second).hasMessageThat().contains("Missing feature factory or integration for ${routeId.key}. Please check your FragmentStore configuration")
    }

    @Test fun `getRenderFactory with feature initialization error`() {
        val screenErrors = mutableListOf<Pair<RouteKey, Throwable>>()
        val environment = RouteEnvironment(
            onScreenError = { key, error -> screenErrors.add(key to error) }
        )

        val routeId = RouteId("test", MainKey(1))
        val features: Map<RouteId<*>, FeatureEvent> = mapOf(
            routeId to FeatureEvent.Failure(
                id = routeId,
                error = IllegalStateException("test")
            )
        )

        val viewFactory = features.getRenderFactory(environment, routeId)
        assertThat(viewFactory).isNull()

        assertThat(screenErrors).hasSize(1)
        assertThat(screenErrors.last().first).isEqualTo(MainKey(1))
        assertThat(screenErrors.last().second).hasMessageThat().contains("Feature failed to initialize: ${routeId.key}")
    }

    @Test fun `getRenderFactory returns valid view factory`() {
        val routeId = RouteId("test", MainKey(1))
        val expectedViewFactory = TestViewFactory<Any>()
        val features: Map<RouteId<*>, FeatureEvent> = mapOf(
            routeId to FeatureEvent.Init(
                id = routeId,
                feature = Feature(
                    state = Observable.empty(),
                    renderFactory = expectedViewFactory
                )
            )
        )

        val viewFactory = features.getRenderFactory(RouteEnvironment(), routeId)
        assertThat(viewFactory).isEqualTo(expectedViewFactory)
    }
}
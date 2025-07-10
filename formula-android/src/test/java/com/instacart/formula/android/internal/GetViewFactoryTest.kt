package com.instacart.formula.android.internal

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureEvent
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.FragmentId
import com.instacart.formula.android.FragmentKey
import com.instacart.formula.android.fakes.MainKey
import com.instacart.testutils.android.TestViewFactory
import io.reactivex.rxjava3.core.Observable
import org.junit.Test

class GetViewFactoryTest {

    @Test fun `getViewFactory with no feature`() {
        val screenErrors = mutableListOf<Pair<FragmentKey, Throwable>>()
        val environment = FragmentEnvironment(
            onScreenError = { key, error -> screenErrors.add(key to error) }
        )

        val fragmentId = FragmentId("test", MainKey(1))
        val features = mapOf<FragmentId, FeatureEvent>()

        val viewFactory = features.getViewFactory(environment, fragmentId)
        assertThat(viewFactory).isNull()

        assertThat(screenErrors).hasSize(1)
        assertThat(screenErrors.last().first).isEqualTo(MainKey(1))
        assertThat(screenErrors.last().second).hasMessageThat().contains("Could not find feature for ${fragmentId.key}")
    }

    @Test fun `getViewFactory with missing binding`() {
        val screenErrors = mutableListOf<Pair<FragmentKey, Throwable>>()
        val environment = FragmentEnvironment(
            onScreenError = { key, error -> screenErrors.add(key to error) }
        )

        val fragmentId = FragmentId("test", MainKey(1))
        val features = mapOf(
            fragmentId to FeatureEvent.MissingBinding(
                id = fragmentId,
            )
        )

        val viewFactory = features.getViewFactory(environment, fragmentId)
        assertThat(viewFactory).isNull()

        assertThat(screenErrors).hasSize(1)
        assertThat(screenErrors.last().first).isEqualTo(MainKey(1))
        assertThat(screenErrors.last().second).hasMessageThat().contains("Missing feature factory or integration for ${fragmentId.key}. Please check your FragmentStore configuration")
    }

    @Test fun `getViewFactory with feature initialization error`() {
        val screenErrors = mutableListOf<Pair<FragmentKey, Throwable>>()
        val environment = FragmentEnvironment(
            onScreenError = { key, error -> screenErrors.add(key to error) }
        )

        val fragmentId = FragmentId("test", MainKey(1))
        val features = mapOf(
            fragmentId to FeatureEvent.Failure(
                id = fragmentId,
                error = IllegalStateException("test")
            )
        )

        val viewFactory = features.getViewFactory(environment, fragmentId)
        assertThat(viewFactory).isNull()

        assertThat(screenErrors).hasSize(1)
        assertThat(screenErrors.last().first).isEqualTo(MainKey(1))
        assertThat(screenErrors.last().second).hasMessageThat().contains("Feature failed to initialize: ${fragmentId.key}")
    }

    @Test fun `getViewFactory returns valid view factory`() {
        val fragmentId = FragmentId("test", MainKey(1))
        val expectedViewFactory = TestViewFactory<Any>()
        val features = mapOf(
            fragmentId to FeatureEvent.Init(
                id = fragmentId,
                feature = Feature(
                    state = Observable.empty(),
                    viewFactory = expectedViewFactory
                )
            )
        )

        val viewFactory = features.getViewFactory(FragmentEnvironment(), fragmentId)
        assertThat(viewFactory).isEqualTo(expectedViewFactory)
    }
}
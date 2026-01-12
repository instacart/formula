package com.instacart.formula.android

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.fakes.MainKey
import com.instacart.formula.android.internal.getRenderFactory
import com.instacart.testutils.android.TestViewFactory
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test

class RenderFactoryTest {

    @Test fun `ViewFactory implements RenderFactory`() {
        val viewFactory: ViewFactory<Any> = TestViewFactory()
        val renderFactory: RenderFactory<Any> = viewFactory
        assertThat(renderFactory).isInstanceOf(RenderFactory::class.java)
        assertThat(renderFactory).isInstanceOf(ViewFactory::class.java)
    }

    @Test fun `Feature can be created with ViewFactory`() {
        val viewFactory = TestViewFactory<String>()
        val feature = Feature(
            state = Observable.just("test"),
            renderFactory = viewFactory
        )
        assertThat(feature.renderFactory).isEqualTo(viewFactory)
    }

    @Test fun `Feature can be created with custom RenderFactory`() {
        val customRenderFactory = object : RenderFactory<String> {}
        val feature = Feature(
            state = Observable.just("test"),
            renderFactory = customRenderFactory
        )
        assertThat(feature.renderFactory).isEqualTo(customRenderFactory)
    }

    @Test fun `getRenderFactory returns ViewFactory`() {
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

        val renderFactory = features.getRenderFactory(RouteEnvironment(), routeId)
        assertThat(renderFactory).isEqualTo(expectedViewFactory)
        assertThat(renderFactory).isInstanceOf(ViewFactory::class.java)
    }

    @Test fun `getRenderFactory returns custom RenderFactory`() {
        val routeId = RouteId("test", MainKey(1))
        val customRenderFactory = object : RenderFactory<Any> {}
        val features: Map<RouteId<*>, FeatureEvent> = mapOf(
            routeId to FeatureEvent.Init(
                id = routeId,
                feature = Feature(
                    state = Observable.empty(),
                    renderFactory = customRenderFactory
                )
            )
        )

        val renderFactory = features.getRenderFactory(RouteEnvironment(), routeId)
        assertThat(renderFactory).isEqualTo(customRenderFactory)
    }

    @Test fun `StateFlow Feature can be created with custom RenderFactory`() {
        val customRenderFactory = object : RenderFactory<Int> {}
        val feature = Feature(
            renderFactory = customRenderFactory
        ) { scope ->
            MutableStateFlow(0)
        }
        assertThat(feature.renderFactory).isEqualTo(customRenderFactory)
    }
}

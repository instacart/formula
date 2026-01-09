package com.instacart.formula.android.compose

import androidx.compose.runtime.Composable
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.Feature
import com.instacart.formula.android.RenderFactory
import com.instacart.formula.android.ViewFactory
import io.reactivex.rxjava3.core.Observable
import org.junit.Test

class ComposeRenderFactoryTest {

    @Test fun `ComposeRenderFactory implements RenderFactory`() {
        val factory = TestComposeRenderFactory()
        assertThat(factory).isInstanceOf(RenderFactory::class.java)
        assertThat(factory).isInstanceOf(ComposeRenderFactory::class.java)
    }

    @Test fun `ComposeRenderFactory does not implement ViewFactory`() {
        val factory = TestComposeRenderFactory()
        assertThat(factory).isNotInstanceOf(ViewFactory::class.java)
    }

    @Test fun `Feature can be created with ComposeRenderFactory`() {
        val factory = TestComposeRenderFactory()
        val feature = Feature(
            state = Observable.just("test"),
            renderFactory = factory
        )
        assertThat(feature.renderFactory).isEqualTo(factory)
    }

    @Test fun `ComposeViewFactory implements both ViewFactory and ComposeRenderFactory`() {
        val factory = TestComposeViewFactory()
        assertThat(factory).isInstanceOf(RenderFactory::class.java)
        assertThat(factory).isInstanceOf(ViewFactory::class.java)
        assertThat(factory).isInstanceOf(ComposeRenderFactory::class.java)
    }

    @Test fun `Feature can be created with ComposeViewFactory`() {
        val factory = TestComposeViewFactory()
        val feature = Feature(
            state = Observable.just("test"),
            renderFactory = factory
        )
        assertThat(feature.renderFactory).isEqualTo(factory)
    }

    @Test fun `ComposeViewFactory initialModel returns null by default`() {
        val factory = TestComposeViewFactory()
        assertThat(factory.initialModel()).isNull()
    }

    @Test fun `ComposeViewFactory initialModel can be overridden`() {
        val factory = object : ComposeViewFactory<String>() {
            override fun initialModel(): String = "initial"

            @Composable
            override fun Content(model: String) {}
        }
        assertThat(factory.initialModel()).isEqualTo("initial")
    }

    private class TestComposeRenderFactory : ComposeRenderFactory<String> {
        @Composable
        override fun Content(model: String) {}
    }

    private class TestComposeViewFactory : ComposeViewFactory<String>() {
        @Composable
        override fun Content(model: String) {}
    }
}

package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RendererTest {

    @Test fun `basic rendering`() {

        val results = mutableListOf<String>()
        val textRenderer = Renderer.create<String> {
            results.add(it)
        }

        textRenderer.render("my title")

        assertThat(results).containsExactly("my title")
    }

    @Test fun `duplicate render models are discarded`() {
        val results = mutableListOf<String>()
        val textRenderer = Renderer.create<String> {
            results.add(it)
        }

        textRenderer.render("my title")
        textRenderer.render("my title")
        textRenderer.render("second title")
        textRenderer.render("my title")

        assertThat(results).containsExactly("my title", "second title", "my title")
    }
}

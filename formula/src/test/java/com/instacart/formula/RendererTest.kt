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

    @Test fun `avoid bad memoization if state update triggers another state update`() {
        val results = mutableListOf<String>()

        lateinit var reference: Renderer<String>
        val renderer = Renderer.create<String> { text ->
            results.add(text)

            if (text == "first") {
                reference.render("second")
            }
        }

        reference = renderer

        renderer.render("first")
        renderer.render("first")

        assertThat(results).containsExactly("first", "second", "first", "second")
    }
}

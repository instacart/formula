package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RendererTest {

    @Test fun `basic rendering`() {
        val subject = TestSubject<String>()
        subject.render("my title")
        subject.assertRenderedValues("my title")
    }

    @Test fun `duplicate render models are discarded`() {
        val subject = TestSubject<String>()
        subject.render("my title")
        subject.render("my title")
        subject.render("second title")
        subject.render("my title")

        subject.assertRenderedValues("my title", "second title", "my title")
    }

    @Test fun `avoid bad memoization if state update triggers another state update`() {
        val subject = TestSubject<String> { render, value ->
            if (value == "first") {
                render("second")
            }
        }
        subject.render("first")
        subject.render("first")

        subject.assertRenderedValues("first", "second", "first", "second")
    }

    @Test fun `render null`() {
        val subject = TestSubject<String?>()
        subject.render(null)
        subject.assertRenderedValues(null)
    }

    @Test fun `render duplicate null emissions ignored`() {
        val subject = TestSubject<String?>()
        subject.render(null)
        subject.render(null)
        subject.assertRenderedValues(null)
    }

    @Test fun `render switches between null and not null values`() {
        val subject = TestSubject<String?>()
        subject.render(null)
        subject.render("value")
        subject.render(null)
        subject.assertRenderedValues(null, "value", null)
    }

    @Test fun `render call triggers another render with same value`() {
        val subject = TestSubject<String?> { render, value ->
            if (value == null) {
                render(null)
            }
        }
        subject.render(null)
        subject.assertRenderedValues(null)
    }

    @Test fun `handling exceptions in rendering`() {
        var crash: Boolean = true
        val subject = TestSubject<String?> { render, value ->
            if (crash) {
                crash = false
                throw IllegalStateException("you can't do this")
            }
        }

        try {
            subject.render(null)
        } catch (e: Exception) {
            // Should log exceptions
        } finally {
            subject.render(null)
        }

        subject.assertRenderedValues(null, null)
    }

    class TestSubject<T>(private val postRender: (Renderer<T>, T) -> Unit = { _, _ -> Unit }) {
        private val results = mutableListOf<T>()
        lateinit var reference: Renderer<T>
        val render = Renderer.create<T> {
            results.add(it)
            postRender(reference, it)
        }

        init {
            reference = render
        }

        fun assertRenderedValues(vararg values: T) {
            assertThat(results).containsExactly(*values)
        }
    }
}

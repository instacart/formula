package com.instacart.formula.android

import android.view.View
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class FeatureViewTest {

    @Test fun `ViewFeatureView implements FeatureView`() {
        val context = RuntimeEnvironment.getApplication()
        val view = View(context)
        val viewFeatureView = ViewFeatureView<String>(
            view = view,
            setOutput = {}
        )
        assertThat(viewFeatureView).isInstanceOf(FeatureView::class.java)
    }

    @Test fun `ViewFeatureView setOutput is called`() {
        val context = RuntimeEnvironment.getApplication()
        val view = View(context)
        var outputReceived: String? = null
        val viewFeatureView = ViewFeatureView<String>(
            view = view,
            setOutput = { outputReceived = it }
        )

        viewFeatureView.setOutput("test-value")
        assertThat(outputReceived).isEqualTo("test-value")
    }

    @Test fun `ViewFeatureView exposes view property`() {
        val context = RuntimeEnvironment.getApplication()
        val expectedView = View(context)
        val viewFeatureView = ViewFeatureView<String>(
            view = expectedView,
            setOutput = {}
        )
        assertThat(viewFeatureView.view).isEqualTo(expectedView)
    }

    @Test fun `ViewFeatureView lifecycleCallbacks is optional`() {
        val context = RuntimeEnvironment.getApplication()
        val view = View(context)
        val viewFeatureView = ViewFeatureView<String>(
            view = view,
            setOutput = {}
        )
        assertThat(viewFeatureView.lifecycleCallbacks).isNull()
    }

    @Test fun `ViewFeatureView lifecycleCallbacks can be provided`() {
        val context = RuntimeEnvironment.getApplication()
        val view = View(context)
        val lifecycleCallback = object : FragmentLifecycleCallback {}
        val viewFeatureView = ViewFeatureView<String>(
            view = view,
            setOutput = {},
            lifecycleCallbacks = lifecycleCallback
        )
        assertThat(viewFeatureView.lifecycleCallbacks).isEqualTo(lifecycleCallback)
    }
}

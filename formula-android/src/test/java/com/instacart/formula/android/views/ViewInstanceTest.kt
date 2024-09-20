package com.instacart.formula.android.views

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.RenderView
import com.instacart.formula.Renderer
import com.instacart.formula.android.ViewInstance
import com.instacart.testutils.android.TestFragmentActivity
import com.instacart.testutils.android.TestFragmentLifecycleCallback
import com.instacart.testutils.android.activity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViewInstanceTest {

    @Test
    fun `feature view with render view`() {
        val renderView = object : RenderView<Any> {
            override val render: Renderer<Any> = Renderer.create {}
        }
        val viewInstance = viewInstance()
        val featureView = viewInstance.featureView(renderView)
        assertThat(featureView.view).isEqualTo(viewInstance.view)
        assertThat(featureView.setOutput).isEqualTo(renderView.render)
        assertThat(featureView.lifecycleCallbacks).isEqualTo(null)
    }

    @Test
    fun `feature view with render view and lifecycle callbacks`() {
        val renderView = object : RenderView<Any> {
            override val render: Renderer<Any> = Renderer.create {}
        }
        val viewInstance = viewInstance()
        val callback = TestFragmentLifecycleCallback()
        val featureView = viewInstance.featureView(renderView, callback)
        assertThat(featureView.lifecycleCallbacks).isEqualTo(callback)
    }

    private fun viewInstance(): ViewInstance {
        val scenario = ActivityScenario.launch(TestFragmentActivity::class.java)
        val view = View(scenario.activity())
        return InflatedViewInstance(view)
    }
}
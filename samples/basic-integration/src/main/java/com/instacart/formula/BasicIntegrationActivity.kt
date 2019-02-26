package com.instacart.formula

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.fragment.FragmentLifecycle
import com.instacart.formula.integration.FragmentFlowRenderView

class BasicIntegrationActivity : FragmentActivity() {

    lateinit var fragmentFlowRenderView: FragmentFlowRenderView

    override fun onCreate(savedInstanceState: Bundle?) {
        fragmentFlowRenderView = FragmentFlowRenderView(this, onLifecycleEvent = {

        })

        super.onCreate(savedInstanceState)
        setContentView(R.layout.basic_integration_activity)
    }

    override fun onDestroy() {
        super.onDestroy()
        fragmentFlowRenderView.dispose()
    }

    override fun onBackPressed() {
        if (!fragmentFlowRenderView.onBackPressed()) {
            super.onBackPressed()
        }
    }
}

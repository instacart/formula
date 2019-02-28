package com.instacart.formula

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.integration.FragmentFlowRenderView

class BasicIntegrationActivity : FragmentActivity() {

    lateinit var fragmentFlowRenderView: FragmentFlowRenderView

    var store = FragmentFlowStore.init {  }

    override fun onCreate(savedInstanceState: Bundle?) {
        fragmentFlowRenderView = FragmentFlowRenderView(this, onLifecycleEvent = store::onLifecycleEffect)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.basic_integration_activity)

        if (savedInstanceState == null) {
            val contract = TaskListContract()
            supportFragmentManager.beginTransaction()
                .add(R.id.activity_content, FormulaFragment.newInstance(contract), contract.tag)
                .commit()
        }
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

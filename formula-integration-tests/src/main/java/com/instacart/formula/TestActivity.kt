package com.instacart.formula

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.integration.FragmentFlowRenderView
import io.reactivex.disposables.CompositeDisposable

class TestActivity : FragmentActivity() {
    private val disposables = CompositeDisposable()

    // Exposed for testing purposes.
    lateinit var fragmentFlowRenderView: FragmentFlowRenderView
    val component = TestComponent()

    override fun onCreate(savedInstanceState: Bundle?) {
        fragmentFlowRenderView = FragmentFlowRenderView(this, onLifecycleEvent = component::onLifecycleEvent)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.basic_integration_activity)

        disposables.add(component.state.subscribe(fragmentFlowRenderView.renderer::render))

        if (savedInstanceState == null) {
            val contract = TaskListContract()
            supportFragmentManager.beginTransaction()
                .add(R.id.activity_content, FormulaFragment.newInstance(contract), contract.tag)
                .commit()
        }
    }

    override fun onDestroy() {
        disposables.clear()
        fragmentFlowRenderView.dispose()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (!fragmentFlowRenderView.onBackPressed()) {
            super.onBackPressed()
        }
    }
}

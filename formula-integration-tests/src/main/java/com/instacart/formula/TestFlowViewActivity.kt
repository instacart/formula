package com.instacart.formula

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.integration.FragmentFlowRenderView
import io.reactivex.disposables.CompositeDisposable

class TestFlowViewActivity : FragmentActivity() {
    private val disposables = CompositeDisposable()

    // Exposed for testing purposes.
    lateinit var fragmentFlowRenderView: FragmentFlowRenderView
    lateinit var viewModel: TestFragmentFlowViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(this).get(TestFragmentFlowViewModel::class.java)
        fragmentFlowRenderView = FragmentFlowRenderView(this, onLifecycleEvent = viewModel::onLifecycleEvent)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity)

        disposables.add(viewModel.state.subscribe(fragmentFlowRenderView.renderer::render))

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

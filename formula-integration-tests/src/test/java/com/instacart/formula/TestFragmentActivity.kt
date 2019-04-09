package com.instacart.formula

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.integration.FragmentFlowRenderView
import io.reactivex.disposables.CompositeDisposable

class TestFragmentActivity : FragmentActivity() {

    private val disposables = CompositeDisposable()

    lateinit var fragmentRenderView: FragmentFlowRenderView
    val component = TestFragmentFlowViewModel()
    lateinit var contract: TestLifecycleContract

    override fun onCreate(savedInstanceState: Bundle?) {

        fragmentRenderView = FragmentFlowRenderView(this, onLifecycleEvent = component::onLifecycleEvent)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity)

        disposables.add(component.state.subscribe(fragmentRenderView.renderer::render))

        if (savedInstanceState == null) {
            val contract = TestLifecycleContract()
            supportFragmentManager.beginTransaction()
                .add(R.id.activity_content, FormulaFragment.newInstance(contract), contract.tag)
                .commit()
            this.contract = contract
        }
    }

    override fun onDestroy() {
        fragmentRenderView.dispose()
        disposables.clear()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (!fragmentRenderView.onBackPressed()) {
            super.onBackPressed()
        }
    }
}

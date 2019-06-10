package com.instacart.formula

import android.os.Bundle
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.integration.FormulaAppCompatActivity

class TestFlowViewActivity : FormulaAppCompatActivity() {
    // Exposed for testing purposes.
    val renderCalls = mutableListOf<Pair<FragmentContract<*>, *>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity)

        if (savedInstanceState == null) {
            val contract = TestContract()
            supportFragmentManager.beginTransaction()
                .add(R.id.activity_content, FormulaFragment.newInstance(contract), contract.tag)
                .commit()
        }
    }
}

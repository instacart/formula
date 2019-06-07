package com.instacart.formula

import android.os.Bundle
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.integration.FormulaAppCompatActivity

class TestFragmentActivity : FormulaAppCompatActivity() {

    lateinit var contract: TestLifecycleContract

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity)

        if (savedInstanceState == null) {
            val contract = TestLifecycleContract()
            supportFragmentManager.beginTransaction()
                .add(R.id.activity_content, FormulaFragment.newInstance(contract), contract.tag)
                .commit()
            this.contract = contract
        }
    }
}

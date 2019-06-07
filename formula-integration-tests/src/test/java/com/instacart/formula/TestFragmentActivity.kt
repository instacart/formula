package com.instacart.formula

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.fragment.FormulaFragment

class TestFragmentActivity : FragmentActivity() {

    lateinit var contract: TestLifecycleContract

    override fun onCreate(savedInstanceState: Bundle?) {
        FormulaAndroid.onPreCreate(this, savedInstanceState)
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

    override fun onBackPressed() {
        if (!FormulaAndroid.onBackPressed(this)) {
            super.onBackPressed()
        }
    }
}

package com.instacart.formula

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.fragment.FragmentContract

class TestFlowViewActivity : FragmentActivity() {
    // Exposed for testing purposes.
    val renderCalls = mutableListOf<Pair<FragmentContract<*>, *>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        FormulaAndroid.onPreCreate(this, savedInstanceState)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity)

        if (savedInstanceState == null) {
            val contract = TaskListContract()
            supportFragmentManager.beginTransaction()
                .add(R.id.activity_content, FormulaFragment.newInstance(contract), contract.tag)
                .commit()
        }
    }

    override fun onBackPressed() {
        if (!FormulaAndroid.onBackPressed(this)) {
            super.onBackPressed()
        }
    }
}
